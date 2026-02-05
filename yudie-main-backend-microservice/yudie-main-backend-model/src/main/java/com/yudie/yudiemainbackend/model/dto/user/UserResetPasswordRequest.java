package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 重置密码请求（忘记密码的情况下）
 * @author: xiaorui
 * @date: 2025-05-22 15:27
 **/
@Data
public class UserResetPasswordRequest implements Serializable {

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码
     */
    private String code;

    private static final long serialVersionUID = 1L;

}
