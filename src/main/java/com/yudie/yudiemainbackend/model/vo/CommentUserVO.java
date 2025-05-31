package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 评论用户VO
 * @author: xiaorui
 * @date: 2025-05-29 22:17
 **/
@Data
public class CommentUserVO implements Serializable {

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

    private static final long serialVersionUID = 1L;

}
