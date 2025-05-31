package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 关注人/粉丝VO
 * @author: xiaorui
 * @date: 2025-05-28 20:18
 **/
@Data
public class FollowersAndFansVO implements Serializable {

    /**
     * 粉丝数量
     */
    private Long fansCount;
    /**
     * 关注数量
     */
    private Long followCount;

    private static final long serialVersionUID = 1L;
}