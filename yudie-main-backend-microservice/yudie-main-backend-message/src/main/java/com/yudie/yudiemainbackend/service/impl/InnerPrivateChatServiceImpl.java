package com.yudie.yudiemainbackend.service.impl;

import com.yudie.yudiemainbackend.innerservice.message.InnerPrivateChatService;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.PrivateChatService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class InnerPrivateChatServiceImpl implements InnerPrivateChatService {

    @Resource
    private PrivateChatService privateChatService;

    @Override
    public void updateChatType(long userId, long targetUserId, boolean isFriend) {
        privateChatService.updateChatType(userId, targetUserId, isFriend);
    }

    @Override
    public void handlePrivateChatMessage(ChatMessage chatMessage, Long privateChatId, User sender) {
        privateChatService.handlePrivateChatMessage(chatMessage, privateChatId, sender);
    }
}
