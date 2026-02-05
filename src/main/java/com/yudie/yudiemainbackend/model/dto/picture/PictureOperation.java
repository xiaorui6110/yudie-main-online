package com.yudie.yudiemainbackend.model.dto.picture;

import lombok.Data;

import java.util.List;

/**
 * @description: 图片操作请求
 * @author: xiaorui
 * @date: 2025-05-23 21:42
 **/
@Data
public class PictureOperation {

    /**
     * id
     */
    private List<Long> ids;

    /**
     * 操作类型
     */
    private long operationType;

}
