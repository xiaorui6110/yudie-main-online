package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 空间成员响应类
 * @author: siri
 * @date: 2025-05-26 19:59
 **/
@Data
public class SpaceUserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 空间信息
     */
    private SpaceVO space;

    private static final long serialVersionUID = 1L;

}
