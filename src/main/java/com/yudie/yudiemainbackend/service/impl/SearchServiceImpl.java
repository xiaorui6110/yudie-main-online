package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.domain.Page;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.model.dto.search.SearchRequest;
import com.yudie.yudiemainbackend.model.entity.*;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.*;
import com.yudie.yudiemainbackend.mapper.HotSearchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


/**
* @author xiaorui
* @description 针对表【hot_search(热门搜索记录表)】的数据库操作Service实现
* @createDate 2025-05-30 09:53:07
*/
@Slf4j
@Service
public class SearchServiceImpl extends ServiceImpl<HotSearchMapper, HotSearch>
    implements SearchService {

    private static final String HOT_SEARCH_CACHE_KEY = "hot_search:%s";

    private static final long CACHE_EXPIRE_TIME = 15 * 60;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private HotSearchMapper hotSearchMapper;

    @Resource
    private PictureService pictureService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

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
     * 执行搜索
     *
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    @Override
    public Page<?> doSearch(SearchRequest searchRequest, HttpServletRequest request) {
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
                return searchPicture(searchRequest, request);
            case "user":
                return searchUser(searchRequest, request);
            case "post":
                return searchPost(searchRequest, request);
            case "space":
                return searchSpace(searchRequest, request);
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
            // 查找是否存在该搜索关键词记录
            HotSearch searchKeyword = hotSearchMapper.selectOne(
                    new QueryWrapper<HotSearch>()
                            .eq("keyword", searchText)
                            .eq("type", type)
            );
            if (searchKeyword != null) {
                // 更新搜索关键词记录
                searchKeyword.setCount(searchKeyword.getCount() + 1);
                searchKeyword.setLastUpdateTime(new Date());
            } else {
                // 新增搜索关键词记录
                searchKeyword = new HotSearch();
                searchKeyword.setKeyword(searchText);
                searchKeyword.setType(type);
                searchKeyword.setCount(1L);
                searchKeyword.setLastUpdateTime(new Date());
                searchKeyword.setCreateTime(new Date());
                searchKeyword.setUpdateTime(new Date());
            }
            // 保存搜索关键词记录
            hotSearchMapper.insertOrUpdate(searchKeyword);
        } catch (Exception e) {
            log.error("记录搜索关键词失败", e);
        }
    }

    /**
     * 搜索图片
     * @param searchRequest 搜索请求
     */
    private Page<PictureVO> searchPicture(SearchRequest searchRequest, HttpServletRequest request) {
        String searchText = searchRequest.getSearchText();
        int current = searchRequest.getCurrent();
        int size = searchRequest.getPageSize();
        // 获取搜索图片 id 列表
        List<Long> pictureIds = this.baseMapper.searchPicture(searchText);
        if (pictureIds == null || pictureIds.isEmpty()) {
           return new PageImpl<>(Collections.emptyList());
        }
        // 计算分页参数
        int total = pictureIds.size();
        // 转换为0-based索引
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        // 获取当前页的 id 子集
        List<Long> pageIds = pictureIds.subList(start, end);
        // 将图片 id 列表转换为图片对象列表
        List<PictureVO> pictureVOList = pageIds.stream()
                .map(id -> pictureService.getPictureVOById(id, request))
                .collect(Collectors.toList());
        // 创建Spring Data分页对象
        Pageable pageable = PageRequest.of(current - 1, size);
        return new PageImpl<>(pictureVOList, pageable, total);
    }

    /**
     * 搜索用户
     * @param searchRequest 搜索请求
     */
    private Page<UserVO> searchUser(SearchRequest searchRequest, HttpServletRequest request) {
        String searchText = searchRequest.getSearchText();
        int current = searchRequest.getCurrent();
        int size = searchRequest.getPageSize();
        // 获取搜索用户 id 列表
        List<Long> userIds = this.baseMapper.searchUser(searchText);
        if (userIds == null || userIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 计算分页参数
        int total = userIds.size();
        // 转换为0-based索引
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        // 获取当前页的 id 子集
        List<Long> pageIds = userIds.subList(start, end);
        // 将用户 id 列表转换为用户对象列表
        List<UserVO> userVOList = pageIds.stream()
                .map(id -> userService.getUserVOById(id))
                .collect(Collectors.toList());
        // 创建Spring Data分页对象
        Pageable pageable = PageRequest.of(current - 1, size);
        return new PageImpl<>(userVOList, pageable, total);
    }

    /**
     * 搜索帖子
     * @param searchRequest 搜索请求
     */
    private Page<Post> searchPost(SearchRequest searchRequest, HttpServletRequest request) {
        String searchText = searchRequest.getSearchText();
        int current = searchRequest.getCurrent();
        int size = searchRequest.getPageSize();
        // 获取搜索帖子 id 列表
        List<Long> postIds = this.baseMapper.searchPost(searchText);
        if (postIds == null || postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 计算分页参数
        int total = postIds.size();
        // 转换为0-based索引
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        // 获取当前页的 id 子集
        List<Long> pageIds = postIds.subList(start, end);
        // 将帖子 id 列表转换为帖子对象列表
        List<Post> postList = pageIds.stream()
                .map(id -> postService.getById(id))
                .collect(Collectors.toList());
        // 创建Spring Data分页对象
        Pageable pageable = PageRequest.of(current - 1, size);
        return new PageImpl<>(postList, pageable, total);
    }

    /**
     * 搜索空间
     * @param searchRequest 搜索请求
     */
    private Page<SpaceVO> searchSpace(SearchRequest searchRequest, HttpServletRequest request) {
        String searchText = searchRequest.getSearchText();
        int current = searchRequest.getCurrent();
        int size = searchRequest.getPageSize();
        // 获取搜索空间 id 列表
        List<Long> spaceIds = this.baseMapper.searchSpace(searchText);
        if (spaceIds == null || spaceIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 计算分页参数
        int total = spaceIds.size();
        // 转换为0-based索引
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        // 获取当前页的 id 子集
        List<Long> pageIds = spaceIds.subList(start, end);
        // 将空间 id 列表转换为空间对象列表
        List<SpaceVO> spaceVOList = pageIds.stream()
                .map(id -> spaceService.getSpaceVOById(id, request))
                .collect(Collectors.toList());
        // 创建Spring Data分页对象
        Pageable pageable = PageRequest.of(current - 1, size);
        return new PageImpl<>(spaceVOList, pageable, total);

    }

}




