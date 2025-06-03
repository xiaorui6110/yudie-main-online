package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.dto.privatechat.PrivateChatQueryRequest;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.PrivateChat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
* @author xiaorui
* @description 针对表【private_chat(私聊表)】的数据库操作Service
* @createDate 2025-05-31 11:31:29
*/
public interface PrivateChatService extends IService<PrivateChat> {

    /**
     * 获取用户私聊列表
     * @param userId 用户ID
     * @param current 当前页
     * @param size 页数
     * @return 私聊列表
     */
    Page<PrivateChat> getUserPrivateChats(long userId, long current, long size);

    /**
     * 创建或更新私聊
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param lastMessage 内容
     * @return 私聊
     */
    PrivateChat createOrUpdatePrivateChat(long userId, long targetUserId, String lastMessage);

    /**
     * 检查是否是好友关系（双向关注）
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 是否是好友关系
     */
    boolean checkIsFriend(long userId, long targetUserId);

    /**
     * 更新聊天类型
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param isFriend 是否是好友关系
     */
    void updateChatType(long userId, long targetUserId, boolean isFriend);

    /**
     * 获取查询条件
     * @param privateChatQueryRequest 私聊查询请求
     * @param loginUser 登录用户
     * @return 查询条件
     */
    QueryWrapper<PrivateChat> getQueryWrapper(PrivateChatQueryRequest privateChatQueryRequest, User loginUser);

    /**
     * 分页查询
     * @param page 页数
     * @param queryWrapper 查询条件
     * @param request 请求
     * @return 分页查询结果
     */
    Page<PrivateChat> page(Page<PrivateChat> page, QueryWrapper<PrivateChat> queryWrapper, HttpServletRequest request);

    /**
     * 获取私聊历史记录
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param page 页数
     * @param size 页大小
     * @return 私聊历史记录
     */
    Page<ChatMessage> getPrivateChatHistory(Long userId, Long targetUserId, Long page, Long size);

    /**
     * 增加用户的未读消息数
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param isUser 是否增加用户的未读消息数（true增加用户的，false增加目标用户的）
     */
    void incrementUnreadCount(long userId, long targetUserId, boolean isUser);

    /**
     * 清除用户的未读消息数
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param isSender 是否清除用户的未读消息数（true清除用户的，false清除目标用户的）
     */
    void clearUnreadCount(long userId, long targetUserId, boolean isSender);

    /**
     * 处理私聊消息
     * @param chatMessage 私聊消息
     * @param privateChatId 私聊ID
     * @param sender 发送者
     */
    void handlePrivateChatMessage(ChatMessage chatMessage, Long privateChatId, User sender);

    /**
     * 删除私聊
     * @param privateChatId 私聊ID
     * @param loginUser 登录用户
     * @return 是否删除成功
     */
    boolean deletePrivateChat(Long privateChatId, User loginUser);

    /**
     * 更新私聊名称
     * @param privateChatId 私聊ID
     * @param chatName 名称
     * @param loginUser 登录用户
     */
    void updateChatName(Long privateChatId, String chatName, User loginUser);

}
