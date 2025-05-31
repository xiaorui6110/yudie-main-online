package com.yudie.yudiemainbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 通用的删除请求类
 * @author: xiaorui
 * @date: 2025-05-20 17:01
 **/
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;

}
