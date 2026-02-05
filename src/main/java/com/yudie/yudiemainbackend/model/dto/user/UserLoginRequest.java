package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户登录请求
 * @author: xiaorui
 * @date: 2025-05-22 10:28
 **/
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -1128916361938530688L;

    /**
     * 账号或邮箱
     */
    private String accountOrEmail;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 验证码
     */
    private String verifyCode;

    /**
     * 验证码ID
     */
    private String serververifycode;

}
