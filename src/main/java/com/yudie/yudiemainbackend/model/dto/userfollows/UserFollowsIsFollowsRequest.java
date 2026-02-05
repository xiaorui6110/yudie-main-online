package com.yudie.yudiemainbackend.model.dto.userfollows;

import lombok.Data;

/**
 * @description: 用户是否关注请求
 * @author: xiaorui
 * @date: 2025-05-28 20:14
 **/
@Data
public class UserFollowsIsFollowsRequest {

    /**
     * 关注者的用户 ID
     */
    private Long followerId;

    /**
     * 被关注者的用户 ID
     */
    private Long followingId;

}
