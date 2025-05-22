package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 修改邮箱请求
 * @author: siri
 * @date: 2025-05-22 15:27
 **/
@Data
public class UserChangeEmailRequest implements Serializable {

    /**
     * 新邮箱
     */
    private String newEmail;

    /**
     * 验证码
     */
    private String code;

    private static final long serialVersionUID = 1L;

}
