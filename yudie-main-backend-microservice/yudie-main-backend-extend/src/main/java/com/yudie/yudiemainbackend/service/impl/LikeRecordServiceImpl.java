package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.innerservice.picture.InnerPictureService;
import com.yudie.yudiemainbackend.innerservice.post.InnerPostService;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserService;
import com.yudie.yudiemainbackend.mapper.LikeRecordMapper;
import com.yudie.yudiemainbackend.model.dto.like.LikeQueryRequest;
import com.yudie.yudiemainbackend.model.dto.like.LikeRequest;
import com.yudie.yudiemainbackend.model.entity.LikeRecord;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.LikeRecordVO;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.service.LikeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【like_record(点赞记录表)】的数据库操作Service实现
* @createDate 2025-05-30 14:27:05
*/
@Slf4j
@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord>
    implements LikeRecordService{

    @Lazy
    @Resource
    private InnerPictureService pictureService;

    @Lazy
    @Resource
    private InnerPostService postService;

    @Resource
    private InnerUserService userService;

    @Override
    @Async("asyncExecutor")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Boolean> doLike(LikeRequest likeRequest, Long userId) {
        try {
            Long targetId = likeRequest.getTargetId();
            Integer targetType = likeRequest.getTargetType();
            Boolean isLiked = likeRequest.getIsLiked();
            // 参数校验
            if (targetId == null || targetType == null || isLiked == null || userId == null) {
                log.error("Invalid parameters: targetId={}, targetType={}, isLiked={}, userId={}",
                        targetId, targetType, isLiked, userId);
                return CompletableFuture.completedFuture(false);
            }
            // 校验目标类型，只允许 1-图片和 2-帖子
            final int pictureType = 1;
            final int postType = 2;
            if (targetType != pictureType && targetType != postType) {
                log.error("Invalid target type: {}", targetType);
                return CompletableFuture.completedFuture(false);
            }
            // 获取目标内容所属用户ID
            Long targetUserId = getTargetUserId(targetId, targetType);
            if (targetUserId == null) {
                log.error("Target content not found: targetId={}, targetType={}", targetId, targetType);
                return CompletableFuture.completedFuture(false);
            }
            // 查询当前点赞状态
            QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId)
                    .eq("targetId", targetId)
                    .eq("targetType", targetType);
            LikeRecord oldLikeRecord = this.getOne(queryWrapper);
            // 处理点赞记录
            if (oldLikeRecord == null) {
                if (isLiked) {
                    // 首次点赞
                    LikeRecord likeRecord = new LikeRecord();
                    likeRecord.setUserId(userId);
                    likeRecord.setTargetId(targetId);
                    likeRecord.setTargetType(targetType);
                    // 设置目标内容所属用户ID
                    likeRecord.setTargetUserId(targetUserId);
                    likeRecord.setIsLiked(true);
                    likeRecord.setFirstLikeTime(new Date());
                    likeRecord.setLastLikeTime(new Date());
                    // 未读状态
                    likeRecord.setIsRead(0);
                    this.save(likeRecord);
                    updateLikeCount(targetId, targetType, 1);
                }
            } else {
                if (!isLiked.equals(oldLikeRecord.getIsLiked())) {
                    // 更新点赞状态
                    oldLikeRecord.setIsLiked(isLiked);
                    oldLikeRecord.setLastLikeTime(new Date());
                    // 更新目标内容所属用户ID
                    oldLikeRecord.setTargetUserId(targetUserId);
                    // 如果是重新点赞，设置为未读
                    if (isLiked) {
                        oldLikeRecord.setIsRead(0);
                    }
                    this.updateById(oldLikeRecord);
                    updateLikeCount(targetId, targetType, isLiked ? 1 : -1);
                }
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error in doLike: ", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 获取目标内容所属用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 目标内容所属用户ID
     */
    private Long getTargetUserId(Long targetId, Integer targetType) {
        try {
            switch (targetType) {
                // 图片
                case 1:
                    Picture picture = pictureService.getById(targetId);
                    return picture != null ? picture.getUserId() : null;
                // 帖子
                case 2:
                    Post post = postService.getById(targetId);
                    return post != null ? post.getUserId() : null;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error getting target user id: ", e);
            return null;
        }
    }

    /**
     * 更新点赞数
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param delta 点赞数变化量
     */
    private void updateLikeCount(Long targetId, Integer targetType, int delta) {
        switch (targetType) {
            // 图片
            case 1:
                pictureService.update()
                        .setSql("likeCount = likeCount + " + delta)
                        .eq("id", targetId)
                        .ge("likeCount", -delta)
                        .update();
                break;
            // 帖子
            case 2:
                postService.update()
                        .setSql("likeCount = likeCount + " + delta)
                        .eq("id", targetId)
                        .ge("likeCount", -delta)
                        .update();
                break;
            default:
                log.error("Unsupported target type: {}", targetType);
        }
    }

    /**
     * 获取并清空未读点赞记录
     * @param userId 用户ID
     * @return 未读点赞记录列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<LikeRecordVO> getAndClearUnreadLikes(Long userId) {
        // 1. 获取未读点赞记录
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetUserId", userId)
                .eq("isRead", 0)
                .eq("isLiked", true)
                .ne("userId", userId)
                .orderByDesc("lastLikeTime")
                // 限制最多返回50条数据
                .last("LIMIT 50");

        List<LikeRecord> unreadLikes = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadLikes)) {
            return new ArrayList<>();
        }
        // 2. 批量更新为已读
        List<Long> likeIds = unreadLikes.stream()
                .map(LikeRecord::getId)
                .collect(Collectors.toList());
        this.update(new UpdateWrapper<LikeRecord>()
                .set("isRead", 1)
                .in("id", likeIds));
        return convertToVOList(unreadLikes);
    }

    /**
     * 将点赞记录列表转换为VO列表
     * @param likeRecords 点赞记录列表
     * @return 点赞记录VO列表
     */
    private List<LikeRecordVO> convertToVOList(List<LikeRecord> likeRecords) {
        if (CollUtil.isEmpty(likeRecords)) {
            return new ArrayList<>();
        }
        return likeRecords.stream().map(like -> {
            LikeRecordVO vo = new LikeRecordVO();
            BeanUtils.copyProperties(like, vo);
            // 设置点赞用户信息
            User likeUser = userService.getById(like.getUserId());
            if (likeUser != null) {
                vo.setUser(userService.getUserVO(likeUser));
            }
            // 根据类型获取目标内容
            switch (like.getTargetType()) {
                // 图片
                case 1:
                    Picture picture = pictureService.getById(like.getTargetId());
                    if (picture != null) {
                        PictureVO pictureVO = PictureVO.objToVo(picture);
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            pictureVO.setUser(userService.getUserVO(pictureUser));
                        }
                        vo.setTarget(pictureVO);
                    }
                    break;
                // 帖子
                case 2:
                    Post post = postService.getById(like.getTargetId());
                    if (post != null) {
                        // 设置帖子作者信息
                        User postUser = userService.getById(post.getUserId());
                        if (postUser != null) {
                            post.setUser(userService.getUserVO(postUser));
                        }
                        vo.setTarget(post);
                    }
                    break;
                default:
                    log.error("Unsupported target type: {}", like.getTargetType());
                    break;
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户点赞历史
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 用户点赞历史
     */
    @Override
    public Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, Long userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();
        // 创建分页对象
        Page<LikeRecord> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        // 查询被点赞的记录
        queryWrapper.eq("targetUserId", userId)
                // 排除自己点赞自己的记录;
                .ne("userId", userId);
        // 处理目标类型查询
        Integer targetType = likeQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }
        queryWrapper.orderByDesc("lastLikeTime");
        // 执行分页查询
        Page<LikeRecord> likePage = this.page(page, queryWrapper);
        // 转换结果
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());
        // 构建返回结果
        Page<LikeRecordVO> voPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    /**
     * 判断内容是否被当前用户点赞
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param userId 用户ID
     * @return 是否被当前用户点赞
     */
    @Override
    public boolean isContentLiked(Long targetId, Integer targetType, Long userId) {
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetId", targetId)
                .eq("targetType", targetType)
                .eq("userId", userId)
                .eq("isLiked", true);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 获取未读点赞数
     * @param userId 用户ID
     * @return 未读点赞数
     */
    @Override
    public long getUnreadLikesCount(Long userId) {
        return this.count(new QueryWrapper<LikeRecord>()
                .eq("targetUserId", userId)
                .eq("isRead", 0)
                .eq("isLiked", true)
                .ne("userId", userId));
    }

    /**
     * 清空未读点赞记录
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadLikes(Long userId) {
        this.update(new UpdateWrapper<LikeRecord>()
                .set("isRead", 1)
                .eq("targetUserId", userId)
                .eq("isRead", 0)
                .eq("isLiked", true));
    }

    /**
     * 获取用户点赞历史
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 用户点赞历史
     */
    @Override
    public Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, Long userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();
        // 创建分页对象
        Page<LikeRecord> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        // 查询用户自己的点赞记录
        queryWrapper.eq("userId", userId)
                // 只查询点赞状态为true的记录
                .eq("isLiked", true);
        // 处理目标类型查询
        Integer targetType = likeQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }
        queryWrapper.orderByDesc("lastLikeTime");
        // 执行分页查询
        Page<LikeRecord> likePage = this.page(page, queryWrapper);
        // 转换结果
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());
        // 构建返回结果
        Page<LikeRecordVO> voPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }
}




