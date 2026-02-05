package com.yudie.yudiemainbackend.innerservice.message;

import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;

public interface InnerPrivateChatService {

    void updateChatType(long userId, long targetUserId, boolean isFriend);

    void handlePrivateChatMessage(ChatMessage chatMessage, Long privateChatId, User sender);
}
