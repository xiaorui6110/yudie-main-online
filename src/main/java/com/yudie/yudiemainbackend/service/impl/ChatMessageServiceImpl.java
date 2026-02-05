package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yudie.yudiemainbackend.constant.RedisConstant;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.ChatMessageService;
import com.yudie.yudiemainbackend.mapper.ChatMessageMapper;
import com.yudie.yudiemainbackend.service.SpaceUserService;
import com.yudie.yudiemainbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
* @author xiaorui
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
* @createDate 2025-05-31 11:05:05
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

    private static final Logger log = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    private final ObjectMapper objectMapper;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public ChatMessageServiceImpl() {
        this.objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 以处理日期时间
        objectMapper.registerModule(new JavaTimeModule());
        // 配置日期时间的序列化格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * 获取与指定用户的聊天记录
     * @param userId 当前用户ID
     * @param otherUserId 对方用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    @Override
    public Page<ChatMessage> getUserChatHistory(long userId, long otherUserId, long current, long size) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .or(w -> w.eq("senderId", userId).eq("receiverId", otherUserId))
                        .or(w -> w.eq("senderId", otherUserId).eq("receiverId", userId)))
                .eq("type", 1)
                .orderByDesc("createTime");
        Page<ChatMessage> page = this.page(new Page<>(current, size), queryWrapper);
        page.getRecords().forEach(this::fillMessageInfo);
        return page;
    }

    /**
     * 获取指定图片的聊天记录
     * @param pictureId 图片ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    @Override
    public Page<ChatMessage> getPictureChatHistory(long pictureId, long current, long size) {
        String cacheKey = RedisConstant.PICTURE_CHAT_HISTORY_PREFIX + pictureId + ":" + current + ":" + size;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            try {
                return objectMapper.readValue(cachedValue, new TypeReference<Page<ChatMessage>>() {});
            } catch (Exception e) {
                log.error("Failed to deserialize chat history from cache", e);
            }
        }
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId)
                // 图片评论类型
                .eq("type", 2)
                .eq("isDelete", 0)
                .orderByDesc("createTime");
        Page<ChatMessage> page = this.page(new Page<>(current, size), queryWrapper);
        page.getRecords().forEach(this::fillMessageInfo);
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(page),
                    RedisConstant.CHAT_HISTORY_EXPIRE_TIME + RandomUtil.randomInt(0, 300),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("Failed to serialize chat history to cache", e);
        }

        return page;
    }

    /**
     * 将消息标记为已读
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     */
    @Override
    public void markAsRead(long receiverId, long senderId) {
        this.update()
                .set("status", 1)
                .eq("receiverId", receiverId)
                .eq("senderId", senderId)
                .eq("status", 0)
                .update();
    }

    /**
     * 获取消息的回复列表
     * @param messageId 消息ID
     * @return 回复列表
     */
    @Override
    public List<ChatMessage> getMessageReplies(long messageId) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("replyId", messageId)
                .orderByAsc("createTime");
        List<ChatMessage> replies = this.list(queryWrapper);
        replies.forEach(this::fillMessageInfo);
        return replies;
    }

    /**
     * 获取消息的完整会话
     * @param messageId 消息ID
     * @return 会话消息列表
     */
    @Override
    public List<ChatMessage> getMessageThread(long messageId) {
        ChatMessage message = this.getById(messageId);
        if (message == null) {
            return null;
        }
        Long rootId = message.getRootId() != null ? message.getRootId() : messageId;
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rootId", rootId)
                .or()
                .eq("id", rootId)
                .orderByAsc("createTime");
        List<ChatMessage> thread = this.list(queryWrapper);
        thread.forEach(this::fillMessageInfo);
        return thread;
    }

    /**
     * 发送回复消息
     * @param message 消息实体
     * @param replyToMessageId 回复的消息ID
     * @return 回复消息实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage reply(ChatMessage message, long replyToMessageId) {
        // 获取被回复的消息
        ChatMessage replyToMessage = this.getById(replyToMessageId);
        if (replyToMessage == null) {
            throw new RuntimeException("回复的消息不存在");
        }
        // 检查空间权限
        if (message.getSpaceId() != null) {
            if (!canUserChatInSpace(message.getSenderId(), message.getSpaceId())) {
                throw new RuntimeException("您不是该空间的成员，无法发送消息");
            }
        }
        // 设置回复消息的关联信息
        message.setReplyId(replyToMessageId);
        message.setRootId(replyToMessage.getRootId() != null ? replyToMessage.getRootId() : replyToMessageId);
        // 保存消息
        this.save(message);
        // 填充消息信息
        fillMessageInfo(message);
        return message;
    }

    /**
     * 获取指定空间的聊天记录
     * @param spaceId 空间ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页数据
     */
    @Override
    public Page<ChatMessage> getSpaceChatHistory(long spaceId, long current, long size) {
        String cacheKey = RedisConstant.SPACE_CHAT_HISTORY_PREFIX + spaceId + ":" + current + ":" + size;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            try {
                return objectMapper.readValue(cachedValue, new TypeReference<Page<ChatMessage>>() {});
            } catch (Exception e) {
                log.error("Failed to deserialize chat history from cache", e);
            }
        }
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spaceId", spaceId)
                // 空间聊天类型
                .eq("type", 3)
                .eq("isDelete", 0)
                .orderByDesc("createTime");
        Page<ChatMessage> page = this.page(new Page<>(current, size), queryWrapper);
        page.getRecords().forEach(this::fillMessageInfo);
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(page),
                    RedisConstant.CHAT_HISTORY_EXPIRE_TIME + RandomUtil.randomInt(0, 300),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("Failed to serialize chat history to cache", e);
        }
        return page;
    }

    /**
     * 检查用户是否有权限在指定空间发送消息
     * @param userId 用户ID
     * @param spaceId 空间ID
     * @return true-有权限，false-无权限
     */
    @Override
    public boolean canUserChatInSpace(long userId, long spaceId) {
        return spaceUserService.isSpaceMember(userId, spaceId);
    }

    /**
     * 获取空间的所有成员
     * @param spaceId 空间ID
     * @return 成员列表
     */
    @Override
    public List<User> getSpaceMembers(long spaceId) {
        return spaceUserService.getSpaceMembers(spaceId);
    }

    @Override
    public Page<ChatMessage> getPrivateChatHistory(long privateChatId, long current, long size) {
        String cacheKey = RedisConstant.PRIVATE_CHAT_HISTORY_PREFIX + privateChatId + ":" + current + ":" + size;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            try {
                return objectMapper.readValue(cachedValue, new TypeReference<Page<ChatMessage>>() {});
            } catch (Exception e) {
                log.error("Failed to deserialize chat history from cache", e);
            }
        }
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("privateChatId", privateChatId)
                .eq("type", 1)
                .eq("isDelete", 0)
                .orderByDesc("createTime");
        Page<ChatMessage> page = this.page(new Page<>(current, size), queryWrapper);
        page.getRecords().forEach(this::fillMessageInfo);
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(page),
                    RedisConstant.CHAT_HISTORY_EXPIRE_TIME + RandomUtil.randomInt(0, 300),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("Failed to serialize chat history to cache", e);
        }
        return page;
    }

    /**
     * 填充消息信息
     * @param message 消息实体
     */
    @Override
    public void fillMessageInfo(ChatMessage message) {
        // 填充发送者信息
        User sender = userService.getById(message.getSenderId());
        if (sender != null) {
            // 清除敏感信息
            sender.setUserPassword(null);
            message.setSender(sender);
        }
        // 如果是回复消息，填充被回复的消息信息
        if (message.getReplyId() != null) {
            ChatMessage replyMessage = this.getById(message.getReplyId());
            if (replyMessage != null) {
                // 递归填充回复消息的信息，但要避免无限递归
                if (replyMessage.getReplyId() == null) {
                    fillMessageInfo(replyMessage);
                } else {
                    // 只填充基本信息
                    User replySender = userService.getById(replyMessage.getSenderId());
                    if (replySender != null) {
                        replySender.setUserPassword(null);
                        replyMessage.setSender(replySender);
                    }
                }
                message.setReplyMessage(replyMessage);
            }
        }
    }

    /**
     * 清除相关的聊天记录缓存
     * @param message 消息实体
     */
    private void clearChatHistoryCache(ChatMessage message) {
        if (message.getSpaceId() != null) {
            // 清除空间聊天缓存
            String pattern = RedisConstant.SPACE_CHAT_HISTORY_PREFIX + message.getSpaceId() + ":*";
            clearCacheByPattern(pattern);
        }
        if (message.getPictureId() != null) {
            // 清除图片评论缓存
            String pattern = RedisConstant.PICTURE_CHAT_HISTORY_PREFIX + message.getPictureId() + ":*";
            clearCacheByPattern(pattern);
        }
        if (message.getPrivateChatId() != null) {
            // 清除私聊记录缓存
            String pattern = RedisConstant.PRIVATE_CHAT_HISTORY_PREFIX + message.getPrivateChatId() + ":*";
            clearCacheByPattern(pattern);
        }
    }

    /**
     * 根据pattern清除缓存
     * @param pattern 缓存key的pattern
     */
    private void clearCacheByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    /**
     * 重写保存方法，在保存消息后清除相关缓存
     * @param message 消息实体
     * @return 保存结果
     */
    @Override
    public boolean save(ChatMessage message) {
        boolean result = super.save(message);
        if (result) {
            clearChatHistoryCache(message);
        }
        return result;
    }

    /**
     * 重写更新方法，在更新消息后清除相关缓存
     * @param message 消息实体
     * @return 更新结果
     */
    @Override
    public boolean updateById(ChatMessage message) {
        boolean result = super.updateById(message);
        if (result) {
            clearChatHistoryCache(message);
        }
        return result;
    }

}
