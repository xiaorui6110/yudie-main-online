package com.yudie.yudiemainbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 按照颜色搜索图片请求
 * @author: siri
 * @date: 2025-05-23 21:43
 **/
@Data
public class SearchPictureByColorRequest implements Serializable {

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;

}
