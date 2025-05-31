package com.yudie.yudiemainbackend.common;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 通用的分页请求类
 * @author: xiaorui
 * @date: 2025-05-20 16:59
 **/
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";

}
