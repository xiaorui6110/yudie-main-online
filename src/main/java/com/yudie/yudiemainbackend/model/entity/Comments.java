package com.yudie.yudiemainbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @author xiaorui
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments implements Serializable {

    /**
     * 评论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long commentId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论目标ID
     */
    private Long targetId;

    /**
     * 评论目标类型：1-图片 2-帖子
     */
    private Integer targetType;

    /**
     * 评论目标所属用户ID
     */
    private Long targetUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 0表示顶级评论
     */
    private Long parentCommentId;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 点踩数
     */
    private Long dislikeCount;

    /**
     * 是否已读（0-未读，1-已读）
     */
    private Integer isRead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}