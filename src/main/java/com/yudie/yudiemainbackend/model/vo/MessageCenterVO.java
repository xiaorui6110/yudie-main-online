package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

/**
 * @description: 留言中心VO
 * @author: siri
 * @date: 2025-05-31 10:43
 **/
@Data
public class MessageCenterVO {

    /**
     * 未读消息总数
     */
    private Long totalUnread;

    /**
     * 未读评论数
     */
    private Long unreadComments;

    /**
     * 未读点赞数
     */
    private Long unreadLikes;

    /**
     * 未读分享数
     */
    private Long unreadShares;

}