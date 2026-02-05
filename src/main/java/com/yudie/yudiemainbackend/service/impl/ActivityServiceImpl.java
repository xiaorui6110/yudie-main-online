package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.model.dto.activity.ActivityAddRequest;
import com.yudie.yudiemainbackend.model.dto.activity.ActivityQueryRequest;
import com.yudie.yudiemainbackend.model.dto.post.PostAttachmentRequest;
import com.yudie.yudiemainbackend.model.entity.Activity;
import com.yudie.yudiemainbackend.model.entity.PostAttachment;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.*;
import com.yudie.yudiemainbackend.mapper.ActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.geom.QuadCurve2D;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【activity(活动表)】的数据库操作Service实现
* @createDate 2025-07-10 20:51:00
*/
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity>
    implements ActivityService{

    @Resource
    private UserService userService;

    @Resource
    private PostAttachmentService postAttachmentService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CrawlerManager crawlerManager;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    @Lazy
    private ShareRecordService shareRecordService;

    /**
     * 创建活动（仅管理员）
     * @param activityAddRequest 创建活动请求
     * @param loginUser 用户
     * @return 活动ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addActivity(ActivityAddRequest activityAddRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(activityAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR);
        // 获取活动信息
        String title = activityAddRequest.getTitle();
        String content = activityAddRequest.getContent();
        String coverUrl = activityAddRequest.getCoverUrl();
        Date expireTime = activityAddRequest.getExpireTime();
        List<PostAttachmentRequest> attachments = activityAddRequest.getAttachments();
        // 再校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(title), ErrorCode.PARAMS_ERROR, "标题不能为空");
        ThrowUtils.throwIf(title.length() > 50, ErrorCode.PARAMS_ERROR, "标题长度不能超过50");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(coverUrl), ErrorCode.PARAMS_ERROR, "封面不能为空");
        ThrowUtils.throwIf(expireTime == null, ErrorCode.PARAMS_ERROR, "过期时间不能为空");
        ThrowUtils.throwIf(expireTime.before(new Date()), ErrorCode.PARAMS_ERROR, "过期时间不能早于当前时间");
        // 处理内容中的图片标记
        if (CollUtil.isNotEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                PostAttachmentRequest attach = attachments.get(i);
                // 图片类型
                if (attach.getType() == 1) {
                    String marker = "{img-" + (i + 1) + "}";
                    // 确保 marker 在内容中存在
                    ThrowUtils.throwIf(!content.contains(marker),
                            ErrorCode.PARAMS_ERROR, "图片标记 " + marker + " 未在内容中找到");
                }
            }
        }
        // 2. 创建活动
        Activity activity = new Activity();
        BeanUtil.copyProperties(activityAddRequest, activity);
        activity.setUserId(loginUser.getId());
        // 管理员创建的活动自动通过审核
        activity.setStatus(1);
        // 未过期
        activity.setIsExpired(0);
        activity.setReviewMessage("管理员创建自动通过");
        activity.setCreateTime(new Date());
        boolean result = this.save(activity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建活动失败");
        // 处理附件时，对于图片类型，使用缩略图URL
        if (CollUtil.isNotEmpty(attachments)) {
            List<PostAttachment> postAttachments = attachments.stream()
                    .map(attach -> {
                        PostAttachment attachment = new PostAttachment();
                        BeanUtils.copyProperties(attach, attachment);
                        attachment.setPostId(activity.getId());
                        // 图片类型
                        if (attach.getType() == 1) {
                            String marker = "{img-" + (attachments.indexOf(attach) + 1) + "}";
                            attachment.setMarker(marker);
                            attachment.setPosition(content.indexOf(marker));
                            // 将原始URL转换为缩略图URL
                            String thumbnailUrl = attach.getUrl().replace("/public/", "/thumbnail/");
                            attachment.setUrl(thumbnailUrl);
                        }
                        return attachment;
                    }).collect(Collectors.toList());
            postAttachmentService.saveBatch(postAttachments);
        }
        // 3. 返回活动ID
        return activity.getId();
    }

    /**
     * 审核活动（仅管理员）
     * @param activityId 活动ID
     * @param status 状态
     * @param reviewMessage 审核信息
     * @param loginUser 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewActivity(Long activityId, Integer status, String reviewMessage, User loginUser) {
        Activity activity = this.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 权限校验
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR);
        // 更新审核信息
        Activity updateActivity = new Activity();
        updateActivity.setId(activityId);
        updateActivity.setStatus(status);
        updateActivity.setReviewMessage(reviewMessage);
        boolean result = this.updateById(updateActivity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "审核活动失败");
    }

    /**
     * 分页获取活动列表
     * @param activityQueryRequest 活动查询请求
     * @param loginUser 用户
     * @return 活动列表
     */
    @Override
    public Page<Activity> listActivities(ActivityQueryRequest activityQueryRequest, User loginUser) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = activityQueryRequest.getCurrent();
        long size = activityQueryRequest.getPageSize();
        // 构建查询条件
        QueryWrapper<Activity> activityQueryWrapper = new QueryWrapper<>();
        String searchText = activityQueryRequest.getSearchText();
        if (StrUtil.isNotBlank(searchText)) {
            activityQueryWrapper.like("title", searchText).or().like("content", searchText);
        }
        // 限制查询范围
        boolean isPublic = activityQueryRequest.getIsPublic();
        boolean isAdmin = userService.isAdmin(loginUser);
        // 公共查询或非管理员，只显示已发布的活动
        if (isPublic || !isAdmin) {
            activityQueryWrapper.eq("status", 1);
        } else {
            // 管理员，显示所有活动
            Integer status = activityQueryRequest.getStatus();
            if (status != null) {
                activityQueryWrapper.eq("status", status);
            }
        }
        // 是否只看未过期
        //if (activityQueryRequest.getNotExpired()) {
        //    activityQueryWrapper.eq("isExpired", 0);
        //}
        //activityQueryWrapper.eq("isDeleted", 0);
        // 排序
        activityQueryWrapper.orderByDesc("createTime");
        // 分页
        Page<Activity> activityPage = this.page(new Page<>(current, size), activityQueryWrapper);
        // 填充活动信息（用户、附件等）
        activityPage.getRecords().forEach(this::fillActivityInfo);
        return activityPage;
    }

    /**
     * 获取活动详情（带浏览量统计）
     * @param activityId 活动ID
     * @param loginUser 用户
     * @param request 请求
     * @return 活动详情
     */
    @Override
    public Activity getActivityDetail(Long activityId, User loginUser, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR);
        crawlerManager.detectNormalRequest(request);
        // 获取活动信息
        Activity activity = this.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
        // 增加浏览量
        incrementViewCount(activityId, request);
        // 填充活动信息（用户、附件等）
        List<PostAttachment> attachments = postAttachmentService.list(new QueryWrapper<PostAttachment>()
                .eq("postId", activityId)
                .orderByAsc("position"));
        // 替换内容中的图片标记为缩略图URL
        String content = activity.getContent();
        for (PostAttachment attachment : attachments) {
            if (attachment.getType() == 1 && StrUtil.isNotBlank(attachment.getMarker())) {
                content = content.replace(attachment.getMarker(),
                        String.format("![%s](%s)", attachment.getName(), attachment.getUrl()));
            }
        }
        activity.setContent(content);
        activity.setAttachments(attachments);
        // 填充用户信息
        User user = userService.getById(activity.getUserId());
        activity.setUser(userService.getUserVO(user));
        // 设置点赞和分享状态
        if (loginUser != null) {
            boolean isLiked = likeRecordService.isContentLiked(activity.getId(), 2, loginUser.getId());
            activity.setIsLiked(isLiked ? 1 : 0);
            boolean isShared = shareRecordService.isContentShared(activity.getId(), 2, loginUser.getId());
            activity.setIsShared(isShared ? 1 : 0);
        } else {
            activity.setIsLiked(0);
            activity.setIsShared(0);
        }
        // 获取最新的浏览量
        long realViewCount = getViewCount(activityId);
        activity.setViewCount(realViewCount);
        return activity;
    }

    /**
     * 异步增加活动浏览量
     * @param activityId 活动ID
     * @param request 请求
     */
    @Async("asyncExecutor")
    public void incrementViewCount(Long activityId, HttpServletRequest request) {
        // 检查是否需要增加浏览量
        if (!crawlerManager.detectViewRequest(request, activityId)) {
            return;
        }
        // 使用 Redis 进行计数
        String viewCountKey = String.format("activity:viewCount:%d", activityId);
        String lockKey = String.format("activity:viewCount:lock:%d", activityId);
        try {
            // 获取分布式锁
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                // 增加浏览量
                stringRedisTemplate.opsForValue().increment(viewCountKey);

                // 当浏览量达到一定阈值时，更新数据库
                String viewCountStr = stringRedisTemplate.opsForValue().get(viewCountKey);
                final int VIEW_COUNT_THRESHOLD = 100;
                if (viewCountStr != null && Long.parseLong(viewCountStr) % VIEW_COUNT_THRESHOLD == 0) {
                    this.update()
                            .setSql("viewCount = viewCount + " + viewCountStr)
                            .eq("id", activityId)
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

    /**
     * 获取活动浏览量
     * @param activityId 活动ID
     * @return 浏览量
     */
    @Override
    public long getViewCount(Long activityId) {
        // 先从 Redis 获取增量
        String viewCountKey = String.format("activity:viewCount:%d", activityId);
        String incrementCount = stringRedisTemplate.opsForValue().get(viewCountKey);
        // 从数据库获取基础浏览量
        Activity activity = this.getById(activityId);
        if (activity == null) {
            return 0L;
        }
        // 合并数据库和 Redis 的浏览量
        long baseCount = activity.getViewCount() != null ? activity.getViewCount() : 0L;
        long increment = incrementCount != null ? Long.parseLong(incrementCount) : 0L;
        return baseCount + increment;
    }

    /**
     * 填充活动信息（用户、附件等）
     * @param activity 活动
     */
    @Override
    public void fillActivityInfo(Activity activity) {
        // 填充用户信息
        User user = userService.getById(activity.getUserId());
        if (user != null) {
            activity.setUser(userService.getUserVO(user));
        }
        // 获取实时浏览量（合并 Redis 中的增量）
        long realViewCount = getViewCount(activity.getId());
        activity.setViewCount(realViewCount);
        // 清空内容，只在详情页显示
        activity.setContent(null);
    }

    /**
     * 获取轮播图活动列表（未过期且已审核通过的）
     * @param activityQueryRequest 活动查询请求
     * @return 轮播图活动列表
     */
    @Override
    public Page<Activity> listCarouselActivities(ActivityQueryRequest activityQueryRequest) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        // 已发布
        queryWrapper.eq("status", 1)
                // 未过期
                .eq("isExpired", 0)
                .eq("isDelete", 0)
                .orderByDesc("createTime")
                // 只选择需要的字段
                .select("id", "title", "coverUrl", "expireTime", "isExpired", "viewCount");
        Page<Activity> activityPage = this.page(new Page<>(activityQueryRequest.getCurrent(),
                activityQueryRequest.getPageSize()), queryWrapper);
        // 获取实时浏览量
        activityPage.getRecords().forEach(activity -> {
            long realViewCount = getViewCount(activity.getId());
            activity.setViewCount(realViewCount);
        });
        return activityPage;

    }
}




