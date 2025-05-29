package com.yudie.yudiemainbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.yudie.yudiemainbackend.model.vo.UserVO;
import lombok.Data;
import lombok.extern.log4j.Log4j;

/**
 * 论坛帖子表
 * @author lenovo
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {
    /**
     * 帖子ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发帖用户ID
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签JSON数组
     */
    private String tags;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 状态 0-待审核 1-已发布 2-已拒绝
     */
    private Integer status;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 分享数
     */
    private Long shareCount;

    /**
     * 帖子附件
     */
    @TableField(exist = false)
    private List<PostAttachment> attachments;

    /**
     * 用户信息
     */
    @TableField(exist = false)
    private UserVO user;

    /**
     * 是否点赞
     */
    @TableField(exist = false)
    private Integer isLiked;

    /**
     * 是否分享
     */
    @TableField(exist = false)
    private Integer isShared;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}