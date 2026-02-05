package com.yudie.yudiemainbackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 编辑空间请求
 * @author: xiaorui
 * @date: 2025-05-27 14:47
 **/
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
