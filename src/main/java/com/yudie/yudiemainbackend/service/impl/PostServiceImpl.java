package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.constant.UserConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.model.dto.post.PostAddRequest;
import com.yudie.yudiemainbackend.model.dto.post.PostAttachmentRequest;
import com.yudie.yudiemainbackend.model.dto.post.PostQueryRequest;
import com.yudie.yudiemainbackend.model.entity.*;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.*;
import com.yudie.yudiemainbackend.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【post(论坛帖子表)】的数据库操作Service实现
* @createDate 2025-05-29 15:36:22
*/
@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

    @Resource
    private PostAttachmentService postAttachmentService;

    @Resource
    private CrawlerManager crawlerManager;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserfollowsService userfollowsService;

    @Resource
    private LikeRecordService likeRecordService;

    @Lazy
    @Resource
    private ShareRecordService shareRecordService;


    /**
     * 发布帖子
     * @param postAddRequest 发布帖子请求
     * @param loginUser 用户
     * @return 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPost(PostAddRequest postAddRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(postAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        String title = postAddRequest.getTitle();
        String content = postAddRequest.getContent();
        List<PostAttachmentRequest> attachments = postAddRequest.getAttachments();
        ThrowUtils.throwIf(StrUtil.isBlank(title), ErrorCode.PARAMS_ERROR, "标题不能为空");
        ThrowUtils.throwIf(title.length() > 100, ErrorCode.PARAMS_ERROR, "标题最多 100 字");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "内容不能为空");
        // 处理内容中的图片标记
        if (CollUtil.isNotEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                PostAttachmentRequest attach = attachments.get(i);
                // 1-图片类型
                if (attach.getType() == 1) {
                    String marker = "{img-" + (i + 1) + "}";
                    // 确保 marker 在内容中存在
                    ThrowUtils.throwIf(!content.contains(marker),
                            ErrorCode.PARAMS_ERROR, "图片标记 " + marker + " 未在内容中找到");
                }
            }
        }
        // 2. 创建帖子
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        post.setUserId(loginUser.getId());
        // 0-待审核
        post.setStatus(0);
        boolean success = this.save(post);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "创建帖子失败");
        // 3. 返回结果
        // 处理附件时，对于图片类型，使用缩略图URL
        if (CollUtil.isNotEmpty(attachments)) {
            List<PostAttachment> postAttachments = attachments.stream()
                    .map(attach -> {
                        PostAttachment attachment = new PostAttachment();
                        BeanUtils.copyProperties(attach, attachment);
                        attachment.setPostId(post.getId());
                        if (attach.getType() == 1) {
                            String marker = "{img-" + (attachments.indexOf(attach) + 1) + "}";
                            attachment.setMarker(marker);
                            attachment.setPosition(content.indexOf(marker));
                            // 将原始 URL 转换为缩略图 URL
                            String thumbnailUrl = attach.getUrl().replace("/public/", "/thumbnail/");
                            attachment.setUrl(thumbnailUrl);
                        }
                        return attachment;
                    }).collect(Collectors.toList());
            postAttachmentService.saveBatch(postAttachments);
        }
        return post.getId();
    }

    /**
     * 检测爬虫或恶意请求
     * @param request 请求
     */
    private void crawlerDetect(HttpServletRequest request) {
        crawlerManager.detectNormalRequest(request);
    }

    /**
     * 获取帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @param loginUser 用户
     * @return 帖子列表
     */
    @Override
    public Page<Post> getPostList(PostQueryRequest postQueryRequest, User loginUser) {
        ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        int current = postQueryRequest.getCurrent();
        int size = postQueryRequest.getPageSize();
        // 限制单页大小，防止爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 构建查询条件
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        // 如果不是管理员，只能看到审核通过的帖子
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            // 1-审核通过
            queryWrapper.eq("status", 1);
        }
        // 添加其他查询条件
        if (StrUtil.isNotBlank(postQueryRequest.getCategory())) {
            queryWrapper.eq("category", postQueryRequest.getCategory());
        }
        if (StrUtil.isNotBlank(postQueryRequest.getSearchText())) {
            queryWrapper.and(wrap -> wrap
                    .like("title", postQueryRequest.getSearchText())
                    .or()
                    .like("content", postQueryRequest.getSearchText())
            );
        }
        // 按用户 id 查询
        if (postQueryRequest.getUserId() != null && postQueryRequest.getUserId() > 0) {
            queryWrapper.eq("userId", postQueryRequest.getUserId());
        }
        // 排序
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc("createTime");
        }
        // 执行分页查询
        Page<Post> postPage = this.page(new Page<>(current, size), queryWrapper);
        // 填充帖子信息
        fillPostsInfo(postPage.getRecords(), loginUser);
        return postPage;
    }

    /**
     * 批量填充帖子信息
     * @param posts 帖子
     * @param loginUser 用户
     */
    private void fillPostsInfo(List<Post> posts, User loginUser) {
        if (CollUtil.isEmpty(posts)) {
            return;
        }
        // 获取所有帖子ID
        Set<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toSet());
        // 批量查询所有图片
        Map<Long, List<PostAttachment>> postAttachmentMap = getPostAttachments(postIds);
        // 批量查询用户信息
        Map<Long, User> userMap = getUserMap(posts);
        // 获取登录用户的点赞和分享信息
        Map<Long, Boolean> likeMap = new HashMap<>();
        Map<Long, Boolean> shareMap = new HashMap<>();
        if (loginUser != null) {
            likeMap = getPostIdIsLikedMap(loginUser, postIds);
            shareMap = getPostIdIsSharedMap(loginUser, postIds);
        }
        // 批量获取浏览量
        Map<Long, Long> viewCountMap = new HashMap<>();
        List<String> viewCountKeys = postIds.stream()
                .map(postId -> String.format("post:viewCount:%d", postId))
                .collect(Collectors.toList());
        if (!viewCountKeys.isEmpty()) {
            List<String> redisViewCounts = stringRedisTemplate.opsForValue().multiGet(viewCountKeys);
            int i = 0;
            for (Long postId : postIds) {
                String redisCount = redisViewCounts.get(i++);
                Post post = this.getById(postId);
                long baseCount = post != null && post.getViewCount() != null ? post.getViewCount() : 0L;
                long increment = redisCount != null ? Long.parseLong(redisCount) : 0L;
                viewCountMap.put(postId, baseCount + increment);
            }
        }
        // 填充信息
        for (Post post : posts) {
            // 清空内容，只在详情页显示
            post.setContent(null);
            // 设置第一张图片
            List<PostAttachment> attachments = postAttachmentMap.get(post.getId());
            post.setAttachments(attachments != null ? attachments : Collections.emptyList());
            // 设置用户信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                post.setUser(userService.getUserVO(user));
            }
            // 设置点赞和分享状态
            post.setIsLiked(likeMap.getOrDefault(post.getId(), false) ? 1 : 0);
            post.setIsShared(shareMap.getOrDefault(post.getId(), false) ? 1 : 0);
            // 设置实时浏览量
            post.setViewCount(viewCountMap.getOrDefault(post.getId(), 0L));
        }
    }

    /**
     * 获取帖子的附件信息
     * @param postIds 帖子ID
     * @return 附件信息
     */
    private Map<Long, List<PostAttachment>> getPostAttachments(Set<Long> postIds) {
        List<PostAttachment> allAttachments = postAttachmentService.list(
                new QueryWrapper<PostAttachment>()
                        .in("postId", postIds)
                        // 只查询图片类型
                        .eq("type", 1)
                        .orderByAsc("position")
        );
        Map<Long, List<PostAttachment>> postAttachmentMap = new HashMap<>();
        if (CollUtil.isNotEmpty(allAttachments)) {
            for (PostAttachment attachment : allAttachments) {
                postAttachmentMap.computeIfAbsent(attachment.getPostId(), k -> new ArrayList<>())
                        .add(attachment);
            }
            // 只保留每个帖子的第一张图片
            postAttachmentMap.forEach((postId, attachments) -> {
                if (attachments.size() > 1) {
                    attachments.subList(1, attachments.size()).clear();
                }
            });
        }
        return postAttachmentMap;
    }

    /**
     * 获取用户信息映射
     * @param posts 帖子
     * @return 用户信息
     */
    private Map<Long, User> getUserMap(List<Post> posts) {
        Set<Long> userIds = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        return userService.listByIds(userIds).stream().
                collect(Collectors.toMap(User::getId, user -> user));
    }

    /**
     * 获取帖子的点赞状态映射
     * @param currentUser 用户
     * @param postIds 帖子ID
     * @return 点赞状态
     */
    private Map<Long, Boolean> getPostIdIsLikedMap(User currentUser, Set<Long> postIds) {
        // 使用通用点赞表查询
        QueryWrapper<LikeRecord> likeQueryWrapper = new QueryWrapper<>();
        likeQueryWrapper.in("targetId", postIds)
                .eq("userId", currentUser.getId())
                // 2-帖子类型
                .eq("targetType", 2)
                .eq("isLiked", true);
        List<LikeRecord> likeRecords = likeRecordService.list(likeQueryWrapper);
        return likeRecords.stream()
                .collect(Collectors.toMap(
                        LikeRecord::getTargetId,
                        like -> true,
                        (b1, b2) -> b1
                ));
    }

    /**
     * 获取帖子的分享状态映射
     * @param currentUser 用户
     * @param postIds 帖子ID
     * @return 分享状态
     */
    private Map<Long, Boolean> getPostIdIsSharedMap(User currentUser, Set<Long> postIds) {
        // 查询分享记录
        QueryWrapper<ShareRecord> shareQueryWrapper = new QueryWrapper<>();
        shareQueryWrapper.in("targetId", postIds)
                .eq("userId", currentUser.getId())
                // 2-帖子类型
                .eq("targetType", 2)
                .eq("isShared", true);
        List<ShareRecord> shareRecords = shareRecordService.list(shareQueryWrapper);
        return shareRecords.stream()
                .collect(Collectors.toMap(
                        ShareRecord::getTargetId,
                        share -> true,
                        (b1, b2) -> b1
                ));
    }

    /**
     * 审核帖子
     * @param postId 帖子 id
     * @param status 帖子状态
     * @param message 审核信息
     * @param loginUser 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewPost(Long postId, Integer status, String message, User loginUser) {
        // 参数校验
        Post post = this.getById(postId);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验权限
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        // 更新审核状态
        Post updatePost = new Post();
        updatePost.setId(postId);
        updatePost.setStatus(status);
        updatePost.setReviewMessage(message);
        boolean success = this.updateById(updatePost);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "更新审核状态失败");
    }

    /**
     * 获取我的帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @return 帖子列表
     */
    @Override
    public Page<Post> getMyPostList(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        // 必须是当前用户的帖子
        ThrowUtils.throwIf(postQueryRequest.getUserId() == null, ErrorCode.PARAMS_ERROR);
        queryWrapper.eq("userId", postQueryRequest.getUserId());
        // 构建查询条件
        if (StrUtil.isNotBlank(postQueryRequest.getCategory())) {
            queryWrapper.eq("category", postQueryRequest.getCategory());
        }
        // 查询审核状态
        if (postQueryRequest.getStatus() != null) {
            queryWrapper.eq("status", postQueryRequest.getStatus());
        }
        // 搜索标题和内容
        if (StrUtil.isNotBlank(postQueryRequest.getSearchText())) {
            queryWrapper.and(wrap -> wrap
                    .like("title", postQueryRequest.getSearchText())
                    .or()
                    .like("content", postQueryRequest.getSearchText())
            );
        }
        // 排序
        queryWrapper.orderByDesc("createTime");
        // 执行分页查询
        Page<Post> postPage = this.page(
                new Page<>(postQueryRequest.getCurrent(), postQueryRequest.getPageSize()), queryWrapper);
        // 填充帖子信息
        List<Post> records = postPage.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            // 获取所有帖子ID
            Set<Long> postIds = records.stream().map(Post::getId).collect(Collectors.toSet());
            // 批量查询所有图片并按位置排序
            List<PostAttachment> allAttachments = postAttachmentService.list(
                    new QueryWrapper<PostAttachment>()
                            .in("postId", postIds)
                            // 只查询图片类型
                            .eq("type", 1)
                            .orderByAsc("position")
            );
            // 在内存中取每个帖子的第一张图片
            Map<Long, List<PostAttachment>> postAttachmentMap = new HashMap<>();
            if (CollUtil.isNotEmpty(allAttachments)) {
                for (PostAttachment attachment : allAttachments) {
                    postAttachmentMap.computeIfAbsent(attachment.getPostId(), k -> new ArrayList<>())
                            .add(attachment);
                }
                // 只保留每个帖子的第一张图片
                postAttachmentMap.forEach((postId, attachments) -> {
                    if (attachments.size() > 1) {
                        attachments.subList(1, attachments.size()).clear();
                    }
                });
            }
            // 查询用户信息
            User user = userService.getById(postQueryRequest.getUserId());
            // 填充信息
            records.forEach(post -> {
                // 清空内容，只在详情页显示
                post.setContent(null);
                // 只设置第一张图片
                List<PostAttachment> attachments = postAttachmentMap.get(post.getId());
                post.setAttachments(attachments != null ? attachments : Collections.emptyList());
                // 设置用户信息
                if (user != null) {
                    post.setUser(userService.getUserVO(user));
                }
                // 设置点赞状态为 0（因为是自己的帖子，不需要显示点赞状态）
                post.setIsLiked(0);
            });
        }
        return postPage;
    }

    /**
     * 更新帖子
     * @param post 帖子
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePost(Post post) {
        ThrowUtils.throwIf(post == null || post.getId() == null, ErrorCode.PARAMS_ERROR);
        // 获取原帖子信息
        Post oldPost = this.getById(post.getId());
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        // 保持不变的字段
        post.setUserId(oldPost.getUserId());
        post.setCreateTime(oldPost.getCreateTime());
        post.setLikeCount(oldPost.getLikeCount());
        post.setCommentCount(oldPost.getCommentCount());
        post.setViewCount(oldPost.getViewCount());
        // 更新基本信息，更新后需要重新审核
        post.setStatus(0);
        post.setUpdateTime(new Date());
        // 处理内容中的图片标记
        String content = post.getContent();
        List<PostAttachment> attachments = post.getAttachments();
        if (CollUtil.isNotEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                PostAttachment attach = attachments.get(i);
                // 图片类型
                if (attach.getType() == 1) {
                    String marker = "{img-" + (i + 1) + "}";
                    // 确保 marker 在内容中存在
                    ThrowUtils.throwIf(!content.contains(marker),
                            ErrorCode.PARAMS_ERROR, "图片标记 " + marker + " 未在内容中找到");
                }
            }
        }
        // 更新操作
        boolean success = this.updateById(post);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "帖子更新失败");
        // 更新附件时，对于图片类型，使用缩略图 URL
        if (attachments != null) {
            // 删除原有附件
            postAttachmentService.remove(new QueryWrapper<PostAttachment>()
                    .eq("postId", post.getId()));
            // 保存新附件
            if (!attachments.isEmpty()) {
                attachments.forEach(attach -> {
                    attach.setPostId(post.getId());
                    if (attach.getType() == 1) { // 图片类型
                        String marker = "{img-" + (attachments.indexOf(attach) + 1) + "}";
                        attach.setMarker(marker);
                        attach.setPosition(content.indexOf(marker));
                        // 将原始 URL 转换为缩略图 URL
                        String thumbnailUrl = attach.getUrl().replace("/public/", "/thumbnail/");
                        attach.setUrl(thumbnailUrl);
                    }
                });
                postAttachmentService.saveBatch(attachments);
            }
        }
        return true;
    }

    /**
     * 获取关注用户的帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @param request 请求
     * @return 帖子列表
     */
    @Override
    public Page<Post> getFollowPosts(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 获取当前用户关注的用户ID列表
        QueryWrapper<Userfollows> followsQueryWrapper = new QueryWrapper<>();
        followsQueryWrapper.eq("followerId", loginUser.getId())
                .eq("followStatus", 1);
        List<Userfollows> userFollowsList = userfollowsService.list(followsQueryWrapper);
        if (CollUtil.isEmpty(userFollowsList)) {
            return new Page<>();
        }
        // 获取关注用户的ID
        List<Long> followingIds = userFollowsList.stream()
                .map(Userfollows::getFollowingId)
                .collect(Collectors.toList());
        // 构建查询条件
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("userId", followingIds)
                // 只查询已发布的帖子
                .eq("status", 1)
                .eq("isDelete", 0);
        // 添加搜索条件
        if (StrUtil.isNotBlank(postQueryRequest.getSearchText())) {
            queryWrapper.and(qw -> qw.like("title", postQueryRequest.getSearchText())
                    .or()
                    .like("content", postQueryRequest.getSearchText()));
        }
        // 添加分类条件
        if (StrUtil.isNotBlank(postQueryRequest.getCategory())) {
            queryWrapper.eq("category", postQueryRequest.getCategory());
        }
        queryWrapper.orderByDesc("createTime");
        // 执行分页查询
        Page<Post> postPage = this.page(
                new Page<>(postQueryRequest.getCurrent(), postQueryRequest.getPageSize()),
                queryWrapper
        );
        // 填充帖子信息
        postPage.getRecords().forEach(this::fillPostInfo);
        return postPage;
    }

    /**
     * 获取帖子榜单
     * @param type 榜单类型：1-日榜 2-周榜 3-月榜 4-总榜
     * @return 帖子列表
     */
    @Override
    public List<Post> getTop100Post(Long type) {
        return getTop100Post(type.longValue(), null);
    }

    /**
     * 获取帖子榜单（带请求检测）
     * @param type 日期类型
     * @param request 请求
     * @return 帖子列表
     */
    private List<Post> getTop100Post(long type, HttpServletRequest request) {
        // 如果有请求对象，进行爬虫检测
        if (request != null) {
            crawlerDetect(request);
        }
        // 构建查询条件
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0)
                // 只查询已审核通过的帖子
                .eq("status", 1);
        // 根据类型设置时间范围
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        switch ((int) type) {
            // 日榜
            case 1:
                calendar.add(Calendar.DATE, -1);
                queryWrapper.ge("createTime", calendar.getTime());
                break;
            // 周榜
            case 2:
                calendar.add(Calendar.DATE, -7);
                queryWrapper.ge("createTime", calendar.getTime());
                break;
            // 月榜
            case 3:
                calendar.add(Calendar.MONTH, -1);
                queryWrapper.ge("createTime", calendar.getTime());
                break;
            case 4:
                // 总榜
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 按照浏览量、点赞数、评论数排序
        queryWrapper.orderByDesc("viewCount", "likeCount", "commentCount");
        // 限制返回 100 条
        queryWrapper.last("LIMIT 100");
        List<Post> posts = list(queryWrapper);
        // 填充帖子信息
        posts.forEach(this::fillPostInfo);
        return posts;
    }

    /**
     * 填充帖子信息
     * @param post 帖子
     */
    @Override
    public void fillPostInfo(Post post) {
        // 填充用户信息
        User user = userService.getById(post.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            post.setUser(userVO);
        }
        // 获取帖子附件（只保留第一张图片）
        QueryWrapper<PostAttachment> attachmentQueryWrapper = new QueryWrapper<>();
        attachmentQueryWrapper.eq("postId", post.getId())
                // 只获取图片类型的附件
                .eq("type", 1)
                .orderByAsc("position")
                .last("LIMIT 1");
        List<PostAttachment> attachments = postAttachmentService.list(attachmentQueryWrapper);
        post.setAttachments(attachments);
        // 获取实时浏览量（合并 Redis 中的增量）
        long realViewCount = getViewCount(post.getId());
        post.setViewCount(realViewCount);
        // 清空内容，只在详情页显示
        post.setContent(null);
    }

    /**
     * 获取实时浏览量
     * @param postId 帖子 id
     * @return 实时浏览量
     */
    @Override
    public long getViewCount(Long postId) {
        // 先从 Redis 获取增量
        String viewCountKey = String.format("post:viewCount:%d", postId);
        String incrementCount = stringRedisTemplate.opsForValue().get(viewCountKey);
        // 从数据库获取基础浏览量
        Post post = this.getById(postId);
        if (post == null) {
            return 0L;
        }
        // 合并数据库和 Redis 的浏览量
        long baseCount = post.getViewCount() != null ? post.getViewCount() : 0L;
        long increment = incrementCount != null ? Long.parseLong(incrementCount) : 0L;
        return baseCount + increment;
    }

    /**
     * 获取帖子详情
     * @param id 帖子 id
     * @param loginUser 用户
     * @param request 请求
     * @return 帖子详情
     */
    @Override
    public Post getPostDetail(Long id, User loginUser, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        crawlerDetect(request);
        Post post = this.getById(id);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        incrementViewCount(id, request);
        // 获取附件并按位置排序
        List<PostAttachment> attachments = postAttachmentService.list(new QueryWrapper<PostAttachment>()
                .eq("postId", id)
                .orderByAsc("position"));
        // 替换内容中的图片标记为缩略图URL
        String content = post.getContent();
        for (PostAttachment attachment : attachments) {
            if (attachment.getType() == 1 && StrUtil.isNotBlank(attachment.getMarker())) {
                content = content.replace(attachment.getMarker(),
                        String.format("![%s](%s)", attachment.getName(), attachment.getUrl()));
            }
        }
        post.setContent(content);
        post.setAttachments(attachments);
        User user = userService.getById(post.getUserId());
        post.setUser(userService.getUserVO(user));
        // 设置点赞和分享状态
        if (loginUser != null) {
            boolean isLiked = likeRecordService.isContentLiked(post.getId(), 2, loginUser.getId());
            post.setIsLiked(isLiked ? 1 : 0);
            boolean isShared = shareRecordService.isContentShared(post.getId(), 2, loginUser.getId());
            post.setIsShared(isShared ? 1 : 0);
        } else {
            post.setIsLiked(0);
            post.setIsShared(0);
        }
        // 获取最新的浏览量
        long realViewCount = getViewCount(id);
        post.setViewCount(realViewCount);
        return post;
    }

    /**
     * 异步增加帖子浏览量
     * @param postId 帖子ID
     * @param request 请求
     */
    @Async("asyncExecutor")
    public void incrementViewCount(Long postId, HttpServletRequest request) {
        // 检查是否需要增加浏览量
        if (!crawlerManager.detectViewRequest(request, postId)) {
            return;
        }
        // 使用 Redis 进行计数
        String viewCountKey = String.format("post:viewCount:%d", postId);
        String lockKey = String.format("post:viewCount:lock:%d", postId);
        try {
            // 获取分布式锁
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                // 增加浏览量
                stringRedisTemplate.opsForValue().increment(viewCountKey);
                // 当浏览量达到一定阈值时，更新数据库
                String viewCountStr = stringRedisTemplate.opsForValue().get(viewCountKey);
                // 改为 100，和图片保持一致
                final int MAX_VIEW_COUNT = 100;
                if (viewCountStr != null && Long.parseLong(viewCountStr) % MAX_VIEW_COUNT == 0) {
                    this.update()
                            .setSql("viewCount = viewCount + " + viewCountStr)
                            .eq("id", postId)
                            .update();
                    // 更新后重置 Redis 计数
                    stringRedisTemplate.delete(viewCountKey);
                }
            }
        } finally {
            // 释放锁
            stringRedisTemplate.delete(lockKey);
        }
    }

}




