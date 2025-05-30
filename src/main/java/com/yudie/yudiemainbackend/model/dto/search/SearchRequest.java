package com.yudie.yudiemainbackend.model.dto.search;

import lombok.Data;

/**
 * @description: 搜索请求
 * @author: siri
 * @date: 2025-05-30 10:00
 **/
@Data
public class SearchRequest {

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 搜索类型
     * picture - 图片搜索
     * user - 用户搜索
     * post - 帖子搜索
     * space - 空间搜索
     */
    private String type;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;
}
