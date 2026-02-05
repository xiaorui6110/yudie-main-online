package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service
* @createDate 2025-05-31 11:05:05
*/
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 获取与指定用户的聊天记录
     * @param userId 当前用户ID
     * @param otherUserId 对方用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    Page<ChatMessage> getUserChatHistory(long userId, long otherUserId, long current, long size);

    /**
     * 获取指定图片的聊天记录
     * @param pictureId 图片ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    Page<ChatMessage> getPictureChatHistory(long pictureId, long current, long size);

    /**
     * 将消息标记为已读
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     */
    void markAsRead(long receiverId, long senderId);

    /**
     * 获取消息的回复列表
     * @param messageId 消息ID
     * @return 回复列表
     */
    List<ChatMessage> getMessageReplies(long messageId);

    /**
     * 获取消息的完整会话
     * @param messageId 消息ID
     * @return 会话消息列表
     */
    List<ChatMessage> getMessageThread(long messageId);

    /**
     * 发送回复消息
     * @param message 消息实体
     * @param replyToMessageId 回复的消息ID
     * @return 回复消息实体
     */
    ChatMessage reply(ChatMessage message, long replyToMessageId);

    /**
     * 获取指定空间的聊天记录
     * @param spaceId 空间ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    Page<ChatMessage> getSpaceChatHistory(long spaceId, long current, long size);

    /**
     * 检查用户是否有权限在指定空间发送消息
     * @param userId 用户ID
     * @param spaceId 空间ID
     * @return true-有权限，false-无权限
     */
    boolean canUserChatInSpace(long userId, long spaceId);

    /**
     * 获取空间的所有成员
     * @param spaceId 空间ID
     * @return 成员列表
     */
    List<User> getSpaceMembers(long spaceId);

    /**
     * 获取私聊历史消息
     * @param privateChatId 私聊ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    Page<ChatMessage> getPrivateChatHistory(long privateChatId, long current, long size);

    /**
     * 填充消息信息
     * @param message 消息实体
     */
    void fillMessageInfo(ChatMessage message);
}
