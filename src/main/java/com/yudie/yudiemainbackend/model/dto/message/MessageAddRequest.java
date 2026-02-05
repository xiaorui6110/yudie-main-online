package com.yudie.yudiemainbackend.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 添加留言请求
 * @author: xiaorui
 * @date: 2025-05-31 10:44
 **/
@Data
public class MessageAddRequest implements Serializable {

    /**
     * 留言内容
     */
    private String content;

    /**
     * IP地址
     */
    private String ip;

    private static final long serialVersionUID = 1L;

}