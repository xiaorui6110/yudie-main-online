package com.yudie.yudiemainbackend.model.dto.comments;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 评论查询请求
 * @author: siri
 * @date: 2025-05-29 22:42
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentsQueryRequest extends PageRequest implements Serializable {

    /**
     * 评论目标id
     */
    private Long targetId;

    /**
     * 评论目标类型：1-图片 2-帖子，默认为1(图片)
     */
    private Integer targetType ;

    private static final long serialVersionUID = 1L;

}
