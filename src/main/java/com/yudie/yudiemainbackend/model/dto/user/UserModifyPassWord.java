package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 修改密码请求
 * @author: xiaorui
 * @date: 2025-05-22 15:27
 **/
@Data
public class UserModifyPassWord implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 原密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;
    /**
     * 确认密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 1L;

}
