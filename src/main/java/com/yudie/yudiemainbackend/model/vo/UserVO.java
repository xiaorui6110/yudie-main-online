package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 用户视图（脱敏）
 * @author: xiaorui
 * @date: 2025-05-22 15:03
 **/
@Data
public class UserVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

}
