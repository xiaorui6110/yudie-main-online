package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @description: Ai 聊天VO
 * @author: xiaorui
 * @date: 2025-07-13 10:44
 **/
@Data
public class AiChatVO {

    /**
     * 消息内容
     */
    private String content;

    /**
     * 角色类型（user-用户, assistant-AI助手）
     */
    private String role;

    /**
     * 创建时间
     */
    private Date createTime;
}
