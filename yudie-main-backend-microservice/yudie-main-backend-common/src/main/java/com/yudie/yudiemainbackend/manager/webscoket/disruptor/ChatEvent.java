package com.yudie.yudiemainbackend.manager.webscoket.disruptor;

import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @description: 聊天事件
 * @author: xiaorui
 * @date: 2025-05-31 14:59
 **/
@Data
public class ChatEvent {

    /**
     * 消息内容
     */
    private ChatMessage chatMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 目标ID(图片ID/空间ID/私聊ID)
     */
    private Long targetId;

    /**
     * 目标类型(1-私聊 2-图片聊天室 3-空间聊天)
     */
    private Integer targetType;

    /**
     * 清空事件数据
     */
    public void clear() {
        this.chatMessage = null;
        this.session = null;
        this.user = null;
        this.targetId = null;
        this.targetType = null;
    }
}
