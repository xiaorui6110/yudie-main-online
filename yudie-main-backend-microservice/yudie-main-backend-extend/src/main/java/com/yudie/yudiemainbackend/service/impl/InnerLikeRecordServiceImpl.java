package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.extend.InnerLikeRecordService;
import com.yudie.yudiemainbackend.model.dto.like.LikeRequest;
import com.yudie.yudiemainbackend.model.entity.LikeRecord;
import com.yudie.yudiemainbackend.service.LikeRecordService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@DubboService
public class InnerLikeRecordServiceImpl implements InnerLikeRecordService {
    @Resource
    private LikeRecordService likeRecordService;

    @Override
    public boolean isContentLiked(Long targetId, Integer targetType, Long userId) {
        return likeRecordService.isContentLiked(targetId, targetType, userId);
    }

    @Override
    public long getUnreadLikesCount(Long userId) {
        return likeRecordService.getUnreadLikesCount(userId);
    }

    @Override
    public void clearAllUnreadLikes(Long userId) {
        likeRecordService.clearAllUnreadLikes(userId);
    }

    @Override
    public List<LikeRecord> list(QueryWrapper<LikeRecord> queryWrapper) {
        return likeRecordService.list(queryWrapper);
    }

    @Override
    public CompletableFuture<Boolean> doLike(LikeRequest likeRequest, Long userId) {
        return likeRecordService.doLike(likeRequest, userId);
    }
}
