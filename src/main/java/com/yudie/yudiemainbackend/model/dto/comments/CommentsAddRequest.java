package com.yudie.yudiemainbackend.model.dto.comments;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 添加评论请求
 * @author: xiaorui
 * @date: 2025-05-29 22:40
 **/
@Data
public class CommentsAddRequest implements Serializable {

    /**
     *  用户id
     */
    private Long userId;

    /**
     * 评论目标id
     */
    private Long targetId;

    /**
     * 评论目标类型：1-图片 2-帖子，默认为1(图片)
     */
    private Integer targetType = 1;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父类评论id
     */
    private Long parentCommentId;

    private static final long serialVersionUID = 1L;

}
