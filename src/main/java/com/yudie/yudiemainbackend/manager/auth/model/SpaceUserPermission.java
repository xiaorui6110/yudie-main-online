package com.yudie.yudiemainbackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 空间成员权限
 * @author: xiaorui
 * @date: 2025-05-28 14:45
 **/
@Data
public class SpaceUserPermission implements Serializable {

    /**
     * 权限键
     */
    private String key;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    private static final long serialVersionUID = 1L;

}
