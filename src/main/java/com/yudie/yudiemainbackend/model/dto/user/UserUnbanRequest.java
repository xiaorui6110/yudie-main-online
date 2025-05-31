package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户封禁/解禁请求
 * @author: xiaorui
 * @date: 2025-05-22 15:27
 **/
@Data
public class UserUnbanRequest implements Serializable {

    /**
     * id
     */
    private Long userId;

    /**
     * 操作类型：true - 解禁，false - 封禁
     */
    private Boolean isUnban;

    private static final long serialVersionUID = 1L;

}
