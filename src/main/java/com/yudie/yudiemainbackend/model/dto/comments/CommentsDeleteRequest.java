package com.yudie.yudiemainbackend.model.dto.comments;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 删除评论请求
 * @author: siri
 * @date: 2025-05-29 22:41
 **/
@Data
public class CommentsDeleteRequest implements Serializable {

    /**
     *  评论id
     */
    private Long commentId;

    private static final long serialVersionUID = 1L;

}