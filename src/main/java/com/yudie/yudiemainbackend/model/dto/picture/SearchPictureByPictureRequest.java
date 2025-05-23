package com.yudie.yudiemainbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 以图搜图请求
 * @author: siri
 * @date: 2025-05-23 21:43
 **/
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;

}
