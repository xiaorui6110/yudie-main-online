package com.yudie.yudiemainbackend.model.dto.search;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 搜索请求
 * @author: xiaorui
 * @date: 2025-05-30 10:00
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchRequest  extends PageRequest implements Serializable {

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
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    private static final long serialVersionUID = 1L;

}
