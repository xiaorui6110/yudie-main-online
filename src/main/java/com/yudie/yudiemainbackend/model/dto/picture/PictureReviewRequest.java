package com.yudie.yudiemainbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 图片审核请求
 * @author: siri
 * @date: 2025-05-23 21:42
 **/
@Data
public class PictureReviewRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;

}
