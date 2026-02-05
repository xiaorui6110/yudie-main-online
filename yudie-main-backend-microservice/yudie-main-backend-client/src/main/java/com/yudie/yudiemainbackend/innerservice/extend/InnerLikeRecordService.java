package com.yudie.yudiemainbackend.innerservice.extend;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.dto.like.LikeRequest;
import com.yudie.yudiemainbackend.model.entity.LikeRecord;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InnerLikeRecordService {


    boolean isContentLiked(Long targetId, Integer targetType, Long userId);

    long getUnreadLikesCount(Long userId);

    void clearAllUnreadLikes(Long userId);

    List<LikeRecord> list(QueryWrapper<LikeRecord> queryWrapper);

    CompletableFuture<Boolean> doLike(LikeRequest likeRequest, Long userId);
}
