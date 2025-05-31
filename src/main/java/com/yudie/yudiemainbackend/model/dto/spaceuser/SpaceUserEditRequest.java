package com.yudie.yudiemainbackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 编辑空间成员请求
 * @author: xiaorui
 * @date: 2025-05-27 15:53
 **/
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}

