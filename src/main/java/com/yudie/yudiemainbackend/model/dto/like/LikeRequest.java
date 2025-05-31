package com.yudie.yudiemainbackend.model.dto.like;

import lombok.Data;

/**
 * @description: 点赞请求
 * @author: xiaorui
 * @date: 2025-05-30 14:34
 **/
@Data
public class LikeRequest {

    /**
     * 目标内容ID
     */
    private Long targetId;

    /**
     * 内容类型：1-图片 2-帖子 3-空间
     */
    private Integer targetType;

    /**
     * 是否点赞（0-不点赞，1-点赞）
     */
    private Boolean isLiked;

}
