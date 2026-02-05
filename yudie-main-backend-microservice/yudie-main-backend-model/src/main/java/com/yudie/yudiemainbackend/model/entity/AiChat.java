package com.yudie.yudiemainbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.StringTypeHandler;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息表
 * @author xiaorui
 * @TableName ai_chat
 */
@TableName(value ="ai_chat")
@Data
public class AiChat implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息内容
     */
    @TableField(typeHandler = StringTypeHandler.class)
    private String content;

    /**
     * 角色类型（user-用户, assistant-AI助手）
     * 默认为用户角色
     */
    private String role;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 会话ID
     */
    private Long sessionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * @param userId 用户ID
     * @param content 消息内容
     * @param role 角色类型（user-用户, assistant-AI助手）
     * @param createTime 创建时间
     */
    public AiChat(Long userId, String content, String role, Date createTime) {
        this.userId = userId;
        this.content = content;
        this.role = role;
        this.createTime = createTime;
        this.isDeleted = 0;
        this.sessionId = 0L;
    }

}