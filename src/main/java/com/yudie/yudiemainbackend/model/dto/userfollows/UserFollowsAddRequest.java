package com.yudie.yudiemainbackend.model.dto.userfollows;

import lombok.Data;

/**
 * @description: 用户添加关注请求
 * @author: xiaorui
 * @date: 2025-05-28 20:13
 **/
@Data
public class UserFollowsAddRequest {

    /**
     * 关注者的用户 ID
     */
    private Long followerId;

    /**
     * 被关注者的用户 ID
     */
    private Long followingId;


    /**
     * 关注状态，0 表示取消关注，1 表示关注
     */
    private Integer followStatus;
}
