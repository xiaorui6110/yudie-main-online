package com.yudie.yudiemainbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 聊天消息表
 * @author lenovo
 * @TableName chat_message
 */
@TableName(value ="chat_message")
@Data
public class ChatMessage implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发送者id
     */
    private Long senderId;

    /**
     * 接收者id
     */
    private Long receiverId;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 1-私聊 2-图片聊天室
     */
    private Integer type;

    /**
     * 状态 0-未读 1-已读
     */
    private Integer status;

    /**
     * 回复的消息id
     */
    private Long replyId;

    /**
     * 会话根消息id
     */
    private Long rootId;

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
     * 空间id
     */
    private Long spaceId;

    /**
     * 私聊id
     */
    private Long privateChatId;

    /**
     * 回复的消息内容
     */
    @TableField(exist = false)
    private ChatMessage replyMessage;

    /**
     * 发送者信息
     */
    @TableField(exist = false)
    private User sender;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}