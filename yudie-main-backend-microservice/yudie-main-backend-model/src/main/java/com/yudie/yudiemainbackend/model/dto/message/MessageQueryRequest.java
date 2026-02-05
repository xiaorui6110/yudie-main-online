package com.yudie.yudiemainbackend.model.dto.message;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 查询留言请求
 * @author: xiaorui
 * @date: 2025-07-11 09:28
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageQueryRequest extends PageRequest {

    /**
     * 留言内容
     */
    private String content;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder;

}
