package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @description: 点赞记录VO
 * @author: siri
 * @date: 2025-05-30 14:31
 **/
@Data
public class LikeRecordVO {
    /**
     * 点赞ID
     */
    private Long id;

    /**
     * 最近点赞时间
     */
    private Date lastLikeTime;

    /**
     * 点赞用户信息
     */
    private UserVO user;

    /**
     * 内容类型：1-图片 2-帖子 3-空间
     */
    private Integer targetType;

    /**
     * 被点赞内容所属用户ID
     */
    private Long targetUserId;

    /**
     * 被点赞的内容（根据targetType可能是PictureVO/Post/SpaceVO）
     */
    private Object target;
}
