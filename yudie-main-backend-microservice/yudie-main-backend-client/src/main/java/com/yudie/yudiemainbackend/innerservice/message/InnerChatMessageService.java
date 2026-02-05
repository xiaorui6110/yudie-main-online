package com.yudie.yudiemainbackend.innerservice.message;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;

import java.util.List;

public interface InnerChatMessageService {

    Page<ChatMessage> getPictureChatHistory(long pictureId, long current, long size);

    Page<ChatMessage> getSpaceChatHistory(long spaceId, long current, long size);

    Page<ChatMessage> getPrivateChatHistory(long privateChatId, long current, long size);

    List<User> getSpaceMembers(long spaceId);

    void fillMessageInfo(ChatMessage message);

    boolean save(ChatMessage message);

    boolean canUserChatInSpace(long userId, long spaceId);
}
