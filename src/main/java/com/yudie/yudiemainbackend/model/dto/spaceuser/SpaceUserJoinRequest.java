package com.yudie.yudiemainbackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 申请加入空间请求
 * @author: xiaorui
 * @date: 2025-05-27 15:53
 **/
@Data
public class SpaceUserJoinRequest implements Serializable {

    /**
     * 空间ID
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}