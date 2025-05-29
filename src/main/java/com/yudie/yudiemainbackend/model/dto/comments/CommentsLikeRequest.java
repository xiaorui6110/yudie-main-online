package com.yudie.yudiemainbackend.model.dto.comments;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 评论点赞请求
 * @author: siri
 * @date: 2025-05-29 22:42
 **/
@Data
public class CommentsLikeRequest  implements Serializable {

    /**
     *  评论id
     */
    private Long commentId;

    /**
     *  用户id
     */
    private Long userId;

    /**
     *  点赞评论
     */
    private Long likeCount;

    /**
     *  点踩评论
     */
    private Long dislikeCount;

    private static final long serialVersionUID = 1L;

}

