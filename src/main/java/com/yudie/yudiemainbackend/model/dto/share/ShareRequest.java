package com.yudie.yudiemainbackend.model.dto.share;

import lombok.Data;

/**
 * @description: 分享请求
 * @author: xiaorui
 * @date: 2025-05-30 14:35
 **/
@Data
public class ShareRequest {

    /**
     * 目标内容ID
     */
    private Long targetId;

    /**
     * 内容类型：1-图片 2-帖子
     */
    private Integer targetType;

    /**
     * 是否分享（0-不分享，1-分享）
     */
    private Boolean isShared;

}
