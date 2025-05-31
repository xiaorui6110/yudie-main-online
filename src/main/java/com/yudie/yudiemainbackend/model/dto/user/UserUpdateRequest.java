package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 更新用户信息请求
 * @author: xiaorui
 * @date: 2025-05-22 15:28
 **/
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;

}
