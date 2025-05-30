package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.esdao.EsSearchKeywordDao;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.model.dto.search.SearchRequest;
import com.yudie.yudiemainbackend.model.entity.*;
import com.yudie.yudiemainbackend.model.entity.es.EsSearchKeyword;
import com.yudie.yudiemainbackend.model.enums.SpaceLevelEnum;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.*;
import com.yudie.yudiemainbackend.mapper.HotSearchMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.redisson.api.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.search.sort.SortOrder.DESC;

/**
* @author lenovo
* @description 针对表【hot_search(热门搜索记录表)】的数据库操作Service实现
* @createDate 2025-05-30 09:53:07
*/
@Slf4j
@Service
public class SearchServiceImpl extends ServiceImpl<HotSearchMapper, HotSearch>
    implements SearchService {

    private static final String HOT_SEARCH_CACHE_KEY = "hot_search:%s";

    private static final long CACHE_EXPIRE_TIME = 15 * 60;

    private static final String PICTURE_INDEX = "picture";

    private static final String USER_INDEX = "user";

    private static final String POST_INDEX = "post";

    private static final String SPACE_INDEX = "space";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private HotSearchMapper hotSearchMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private UserService userService;

    @Resource
    private PostAttachmentService postAttachmentService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private EsSearchKeywordDao esSearchKeywordDao;

    /**
     * 获取热门搜索（优先从Redis获取，其次MySQL，最后ES）
     * @param type 搜索类型
     * @param size 搜索关键词数量
     * @return 热门搜索关键词列表
     */
    @Override
    public List<String> getHotSearchKeywords(String type, Integer size) {
        // 构建缓存 Key
        String cacheKey = String.format(HOT_SEARCH_CACHE_KEY, type);
        try {
            // 1. 先从 Redis 获取
            List<String> cachedList = stringRedisTemplate.opsForList().range(cacheKey, 0, size - 1);
            if (cachedList != null && !cachedList.isEmpty()) {
                return cachedList;
            }
            // 2. Redis 没有，从 MySQL 获取
            Date startTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            List<HotSearch> hotSearchList = hotSearchMapper.getHotSearchAfter(type, startTime, size);

            List<String> resultList = new ArrayList<>();
            if (!hotSearchList.isEmpty()) {
                resultList = hotSearchList.stream()
                        .map(HotSearch::getKeyword)
                        .collect(Collectors.toList());
            }
            else {
                // 3. MySQL 没有，从 ES 获取
                List<EsSearchKeyword> keywords = esSearchKeywordDao
                        .findByTypeAndUpdateTimeAfterOrderByCountDesc(type, startTime);

                resultList = keywords.stream()
                        .limit(size)
                        .map(EsSearchKeyword::getKeyword)
                        .collect(Collectors.toList());
            }
            // 如果有结果，更新缓存
            if (!resultList.isEmpty()) {
                updateHotSearchCache(type, resultList);
            }

            return resultList;
        } catch (Exception e) {
            log.error("获取热门搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新热门搜索缓存
     * @param type 搜索类型
     * @param keywords 搜索关键词
     */
    private void updateHotSearchCache(String type, List<String> keywords) {
        // 构建缓存 Key
        String cacheKey = String.format(HOT_SEARCH_CACHE_KEY, type);
        try {
            // 使用管道批量操作
            stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
                connection.multi();
                // 删除旧的缓存
                connection.del(cacheKey.getBytes());
                // 添加新的缓存
                for (String keyword : keywords) {
                    connection.rPush(cacheKey.getBytes(), keyword.getBytes());
                }
                // 设置过期时间（15~20 分钟随机）
                long expireSeconds = CACHE_EXPIRE_TIME + RandomUtil.randomInt(0, 300);
                connection.expire(cacheKey.getBytes(), expireSeconds);
                connection.exec();
                return null;
            });
        } catch (Exception e) {
            log.error("更新热门搜索缓存失败", e);
        }
    }

    /**
     *  执行搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    @Override
    public Page<?> doSearch(SearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        String type = searchRequest.getType();
        // 校验参数
        if (StringUtils.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索关键词不能为空");
        }
        // 记录搜索关键词
        recordSearchKeyword(searchText, type);
        // 执行搜索
        switch (type) {
            case "picture":
                return searchPicture(searchRequest);
            case "user":
                return searchUser(searchRequest);
            case "post":
                return searchPost(searchRequest);
            case "space":
                return searchSpace(searchRequest);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的搜索类型");
        }
    }

    /**
     * 记录搜索关键词
     * @param searchText 搜索关键词
     * @param type 搜索类型
     */
    private void recordSearchKeyword(String searchText, String type) {
        try {
            // 查找是否存在该关键词记录
            EsSearchKeyword keyword = esSearchKeywordDao.findByTypeAndKeyword(type, searchText);

            if (keyword != null) {
                // 更新搜索次数
                keyword.setCount(keyword.getCount() + 1);
                keyword.setUpdateTime(new Date());
            } else {
                // 新增关键词记录
                keyword = new EsSearchKeyword();
                keyword.setKeyword(searchText);
                keyword.setType(type);
                keyword.setCount(1L);
                keyword.setCreateTime(new Date());
                keyword.setUpdateTime(new Date());
            }
            esSearchKeywordDao.save(keyword);
        } catch (Exception e) {
            log.error("记录搜索关键词失败", e);
        }
    }

    /**
     * 搜索图片
     */
    private Page<PictureVO> searchPicture(SearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        Integer current = searchRequest.getCurrent();
        Integer pageSize = searchRequest.getPageSize();
        // 构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                // 搜索条件
                .should(QueryBuilders.matchQuery("name", searchText))
                .should(QueryBuilders.matchQuery("introduction", searchText))
                .should(QueryBuilders.matchQuery("tags", searchText));
        // 尝试将搜索文本转换为图片ID
        try {
            Long pictureId = Long.parseLong(searchText);
            boolQueryBuilder.should(QueryBuilders.termQuery("id", pictureId));
        } catch (NumberFormatException ignored) {
        }
        boolQueryBuilder.minimumShouldMatch(1)
                // 必要条件：已通过审核、未删除、公共图库
                .must(QueryBuilders.termQuery("reviewStatus", 1))
                .must(QueryBuilders.termQuery("isDelete", 0))
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.boolQuery()
                                .mustNot(QueryBuilders.existsQuery("spaceId")))
                        .should(QueryBuilders.termQuery("spaceId", 0))
                );
        // 构建搜索查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(current - 1, pageSize))
                .withSort(SortBuilders.scoreSort().order(DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(DESC))
                .build();
        // 执行搜索
        SearchHits<Picture> searchHits = elasticsearchRestTemplate.search(
                searchQuery,
                Picture.class,
                IndexCoordinates.of(PICTURE_INDEX)
        );
        // 获取搜索结果并转换为PictureVO
        List<PictureVO> pictureVOList = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .map(picture -> {
                    PictureVO pictureVO = PictureVO.objToVo(picture);
                    // 获取并设置脱敏后的用户信息
                    User user = userService.getById(picture.getUserId());
                    if (user != null) {
                        pictureVO.setUser(userService.getUserVO(user));
                    }
                    return pictureVO;
                })
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                pictureVOList,
                PageRequest.of(current - 1, pageSize),
                searchHits.getTotalHits()
        );
    }

    /**
     * 搜索用户
     */
    private Page<UserVO> searchUser(SearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        Integer current = searchRequest.getCurrent();
        Integer pageSize = searchRequest.getPageSize();
        // 构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("userName", searchText))
                .should(QueryBuilders.matchQuery("userAccount", searchText))
                .should(QueryBuilders.matchQuery("userProfile", searchText));
        // 尝试将搜索文本转换为用户ID
        try {
            Long userId = Long.parseLong(searchText);
            boolQueryBuilder.should(QueryBuilders.termQuery("id", userId));
        } catch (NumberFormatException ignored) {
        }

        boolQueryBuilder.minimumShouldMatch(1)
                .must(QueryBuilders.termQuery("isDelete", 0));

        // 构建搜索查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(current - 1, pageSize))
                .withSort(SortBuilders.scoreSort().order(DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(DESC))
                .build();
        // 执行搜索
        SearchHits<User> searchHits = elasticsearchRestTemplate.search(
                searchQuery,
                User.class,
                IndexCoordinates.of(USER_INDEX)
        );
        // 获取搜索结果并转换为UserVO
        List<UserVO> userVOList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(userService::getUserVO)
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(
                userVOList,
                PageRequest.of(current - 1, pageSize),
                searchHits.getTotalHits()
        );
    }

    /**
     * 搜索帖子
     */
    private Page<Post> searchPost(SearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        Integer current = searchRequest.getCurrent();
        Integer pageSize = searchRequest.getPageSize();

        // 构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title", searchText))
                .should(QueryBuilders.matchQuery("content", searchText))
                .should(QueryBuilders.matchQuery("tags", searchText))
                .should(QueryBuilders.matchQuery("category", searchText));

        // 尝试将搜索文本转换为帖子ID
        try {
            Long postId = Long.parseLong(searchText);
            boolQueryBuilder.should(QueryBuilders.termQuery("id", postId));
        } catch (NumberFormatException ignored) {
        }

        boolQueryBuilder.minimumShouldMatch(1)
                .must(QueryBuilders.termQuery("isDelete", 0))
                // 只搜索已发布的帖子
                .must(QueryBuilders.termQuery("status", 1));

        // 修改排序逻辑，加入分享数权重
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(current - 1, pageSize))
                .withSort(SortBuilders.scoreSort().order(DESC))
                // 使用加权排序：点赞数 40%，评论数 30%，浏览量 20%，分享数 10%
                .withSort(SortBuilders.scriptSort(
                        new Script(
                                ScriptType.INLINE,
                                "painless",
                                "doc['likeCount'].value * 0.4 + doc['commentCount'].value * 0.3 + " +
                                        "doc['viewCount'].value * 0.2 + doc['shareCount'].value * 0.1",
                                new HashMap<>()
                        ),
                        ScriptSortBuilder.ScriptSortType.NUMBER
                ).order(DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(DESC))
                .build();

        // 执行搜索
        SearchHits<Post> searchHits = elasticsearchRestTemplate.search(
                searchQuery,
                Post.class,
                IndexCoordinates.of(POST_INDEX)
        );

        // 获取搜索结果
        List<Post> postList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // 批量获取帖子的附件
        if (!postList.isEmpty()) {
            // 获取所有帖子ID
            List<Long> postIds = postList.stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());

            // 批量查询附件
            List<PostAttachment> attachments = postAttachmentService.list(
                    new QueryWrapper<PostAttachment>()
                            .in("postId", postIds)
                            // 只获取图片类型的附件
                            .eq("type", 1)
                            // 按位置排序
                            .orderByAsc("position")
            );

            // 构建帖子ID到附件列表的映射
            Map<Long, List<PostAttachment>> postAttachmentMap = attachments.stream()
                    .collect(Collectors.groupingBy(PostAttachment::getPostId));

            // 填充帖子信息
            postList.forEach(post -> {
                // 获取并设置用户信息
                User user = userService.getById(post.getUserId());
                if (user != null) {
                    post.setUser(userService.getUserVO(user));
                }

                // 清空内容，只在详情页显示
                post.setContent(null);

                // 设置附件（只保留第一张图片）
                List<PostAttachment> postAttachments = postAttachmentMap.get(post.getId());
                if (CollUtil.isNotEmpty(postAttachments)) {
                    // 只保留第一张图片
                    postAttachments = postAttachments.subList(0, 1);
                }
                post.setAttachments(postAttachments != null ? postAttachments : Collections.emptyList());
            });
        }

        return new org.springframework.data.domain.PageImpl<>(
                postList,
                PageRequest.of(current - 1, pageSize),
                searchHits.getTotalHits()
        );
    }

    /**
     * 搜索空间
     */
    private Page<SpaceVO> searchSpace(SearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        Integer current = searchRequest.getCurrent();
        Integer pageSize = searchRequest.getPageSize();

        // 构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("spaceName", searchText));

        // 尝试将搜索文本转换为空间ID
        try {
            Long spaceId = Long.parseLong(searchText);
            boolQueryBuilder.should(QueryBuilders.termQuery("id", spaceId));
        } catch (NumberFormatException ignored) {
        }

        boolQueryBuilder.minimumShouldMatch(1)
                .must(QueryBuilders.termQuery("isDelete", 0))
                // 只搜索团队空间
                .must(QueryBuilders.termQuery("spaceType", 1));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(current - 1, pageSize))
                .withSort(SortBuilders.scoreSort().order(DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(DESC))
                .build();

        SearchHits<Space> searchHits = elasticsearchRestTemplate.search(
                searchQuery,
                Space.class,
                IndexCoordinates.of(SPACE_INDEX)
        );

        List<Space> spaceList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // 填充用户信息并转换为VO
        List<SpaceVO> spaceVOList = new ArrayList<>();
        if (!spaceList.isEmpty()) {
            // 获取所有空间ID
            List<Long> spaceIds = spaceList.stream()
                    .map(Space::getId)
                    .collect(Collectors.toList());

            // 批量查询实际的空间数据
            List<Space> actualSpaces = spaceService.listByIds(spaceIds);
            Map<Long, Space> spaceMap = actualSpaces.stream()
                    .collect(Collectors.toMap(Space::getId, space -> space));

            // 获取所有用户ID
            Set<Long> userIds = spaceList.stream()
                    .map(Space::getUserId)
                    .collect(Collectors.toSet());

            // 批量查询用户信息
            Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.groupingBy(User::getId));

            // 批量查询空间成员数量
            Map<Long, Long> spaceMemberCountMap = spaceList.stream()
                    .collect(Collectors.toMap(
                            Space::getId,
                            space -> {
                                QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
                                queryWrapper.eq("spaceId", space.getId())
                                        .eq("status", 1);  // 只统计已通过的成员
                                return spaceUserService.count(queryWrapper);
                            }
                    ));

            // 转换为VO并填充信息
            spaceVOList = spaceList.stream()
                    .map(space -> {
                        SpaceVO spaceVO = SpaceVO.objToVo(space);

                        // 从数据库获取实际的空间数据
                        Space actualSpace = spaceMap.get(space.getId());
                        if (actualSpace != null) {
                            spaceVO.setTotalSize(actualSpace.getTotalSize());
                            spaceVO.setTotalCount(actualSpace.getTotalCount());
                        }

                        // 填充空间级别相关信息
                        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
                        if (spaceLevelEnum != null) {
                            spaceVO.setMaxSize(spaceLevelEnum.getMaxSize());
                            spaceVO.setMaxCount(spaceLevelEnum.getMaxCount());
                        }

                        // 设置默认值（只在实际值为null时设置）
                        spaceVO.setTotalSize(spaceVO.getTotalSize() != null ? spaceVO.getTotalSize() : 0L);
                        spaceVO.setTotalCount(spaceVO.getTotalCount() != null ? spaceVO.getTotalCount() : 0L);

                        // 填充用户信息
                        Long userId = space.getUserId();
                        if (userIdUserListMap.containsKey(userId)) {
                            User user = userIdUserListMap.get(userId).get(0);
                            UserVO userVO = userService.getUserVO(user);
                            // 设置用户默认值
                            userVO.setUserProfile(userVO.getUserProfile() != null ? userVO.getUserProfile() : "");
                            spaceVO.setUser(userVO);
                        }

                        // 填充成员数量
                        spaceVO.setMemberCount(spaceMemberCountMap.getOrDefault(space.getId(), 0L));

                        return spaceVO;
                    })
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(
                spaceVOList,
                PageRequest.of(current - 1, pageSize),
                searchHits.getTotalHits()
        );
    }


}




