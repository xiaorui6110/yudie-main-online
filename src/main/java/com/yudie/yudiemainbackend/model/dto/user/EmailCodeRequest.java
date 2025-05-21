package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 邮箱验证码请求
 * @author: siri
 * @date: 2025-05-21 20:22
 **/
@Data
public class EmailCodeRequest implements Serializable {

    /**
     * 邮箱地址
     */
    private String userEmail;

    /**
     * 验证码用途：register-注册，resetPassword-重置密码，changeEmail-修改邮箱
     */
    private String type;

    private static final long serialVersionUID = 1L;

}
