package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 图片标签分类列表视图
 * @author: xiaorui
 * @date: 2025-05-26 19:42
 **/
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;

}
