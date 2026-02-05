package com.yudie.yudiemainbackend.model.dto.picture;

import lombok.Data;

/**
 * @description: 图片精选请求
 * @author: xiaorui
 * @date: 2025-07-10 12:34
 **/

@Data
public class PictureFeatureRequest {
    /**
     * 图片id
     */
    private Long id;

    /**
     * 是否精选 0-非精选 1-精选
     */
    private Integer isFeature;
}

