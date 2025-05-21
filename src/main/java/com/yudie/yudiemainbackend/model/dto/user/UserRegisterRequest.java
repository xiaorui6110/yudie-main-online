package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户注册请求
 * @author: siri
 * @date: 2025-05-21 14:51
 **/
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1466798535319564181L;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码
     */
    private String code;

}
