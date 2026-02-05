package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.innerservice.message.InnerChatMessageService;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.ChatMessageService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerChatMessageServiceImpl implements InnerChatMessageService {

    @Resource
    private ChatMessageService chatMessageService;

    @Override
    public Page<ChatMessage> getPictureChatHistory(long pictureId, long current, long size) {
        return chatMessageService.getPictureChatHistory(pictureId, current, size);
    }

    @Override
    public Page<ChatMessage> getSpaceChatHistory(long spaceId, long current, long size) {
        return chatMessageService.getSpaceChatHistory(spaceId, current, size);
    }

    @Override
    public Page<ChatMessage> getPrivateChatHistory(long privateChatId, long current, long size) {
        return chatMessageService.getPrivateChatHistory(privateChatId, current, size);
    }

    @Override
    public List<User> getSpaceMembers(long spaceId) {
        return chatMessageService.getSpaceMembers(spaceId);
    }

    @Override
    public void fillMessageInfo(ChatMessage message) {
        chatMessageService.fillMessageInfo(message);
    }

    @Override
    public boolean save(ChatMessage message) {
        return chatMessageService.save(message);
    }

    @Override
    public boolean canUserChatInSpace(long userId, long spaceId) {
        return chatMessageService.canUserChatInSpace(userId, spaceId);
    }
}
