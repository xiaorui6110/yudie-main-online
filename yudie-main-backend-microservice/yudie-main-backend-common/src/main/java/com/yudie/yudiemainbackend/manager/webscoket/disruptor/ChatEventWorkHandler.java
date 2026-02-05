package com.yudie.yudiemainbackend.manager.webscoket.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.yudie.yudiemainbackend.manager.webscoket.ChatWebSocketServer;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: 聊天事件处理器
 * @author: xiaorui
 * @date: 2025-05-31 15:00
 **/
@Slf4j
@Component
public class ChatEventWorkHandler implements WorkHandler<ChatEvent> {

    @Resource
    @Lazy
    private ChatWebSocketServer chatWebSocketServer;

    @Override
    public void onEvent(ChatEvent event) {
        try {
            ChatMessage chatMessage = event.getChatMessage();
            // 确保消息的目标ID已设置
            switch (event.getTargetType()) {
                // 私聊
                case 1:
                    chatMessage.setPrivateChatId(event.getTargetId());
                    chatWebSocketServer.handlePrivateChatMessage(chatMessage, event.getSession());
                    break;
                // 图片聊天室
                case 2:
                    chatMessage.setPictureId(event.getTargetId());
                    chatWebSocketServer.handlePictureChatMessage(chatMessage, event.getSession());
                    break;
                // 空间聊天
                case 3:
                    chatMessage.setSpaceId(event.getTargetId());
                    chatWebSocketServer.handleSpaceChatMessage(chatMessage, event.getSession());
                    break;
                default:
                    log.error("Unknown target type: {}", event.getTargetType());
            }
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
        } finally {
            // 清空事件数据
            event.clear();
        }
    }
}