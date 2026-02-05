package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.config.DeepSeekConfig;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserService;
import com.yudie.yudiemainbackend.mapper.AiChatMapper;
import com.yudie.yudiemainbackend.model.dto.aichat.AiChatRequest;
import com.yudie.yudiemainbackend.model.dto.aichat.AiChatResponse;
import com.yudie.yudiemainbackend.model.entity.AiChat;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.AiChatVO;
import com.yudie.yudiemainbackend.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【ai_chat(聊天消息表)】的数据库操作Service实现
* @createDate 2025-07-13 10:24:34
*/
@Slf4j
@Service
public class AiChatServiceImpl extends ServiceImpl<AiChatMapper, AiChat>
    implements AiChatService{

    @Resource
    private InnerUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DeepSeekConfig deepSeekConfig;

    @Resource
    private AiChatMapper aiChatMapper;

    /**
     * 聊天信息缓存key
     */
    private static final String CHAT_CACHE_KEY = "chat:messages:%s";

    /**
     * 聊天信息数量缓存key
     */
    private static final String DAILY_MESSAGE_COUNT_KEY = "chat:daily:count:%s:%s";

    /**
     * 每日消息限制
     */
    private static final int DAILY_MESSAGE_LIMIT = 100;

    /**
     * 生成回复
     * @param query 提问
     * @param request 请求
     * @return 回复
     */
    @Override
    public String generateResponse(String query, HttpServletRequest request) {
        // 1.简单校验
        User loginUser = userService.isLogin(request);
        Long userId = loginUser.getId();
        ThrowUtils.throwIf(userId == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 检查是否超过每日限制
        String today = java.time.LocalDate.now().toString();
        String countKey = String.format(DAILY_MESSAGE_COUNT_KEY, userId, today);
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        if (countStr != null) {
            int count = Integer.parseInt(countStr);
            ThrowUtils.throwIf(count >= DAILY_MESSAGE_LIMIT,
                    ErrorCode.OPERATION_ERROR, "已达到今日消息上限(100条)，请明天再来");
        }
        // 2.调用AI接口
        try {
            // 构建请求参数
            AiChatRequest aiChatRequest = buildRequest(query);
            // 执行 HTTP 请求
            HttpResponse execute = HttpUtil.createRequest(Method.POST, deepSeekConfig.getApiUrl())
                    .body(JSONUtil.toJsonStr(aiChatRequest))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                    .header("Accept", "application/json")
                    .execute();
            // 获取并日志记录响应内容
            String response = execute.body();
            log.info("deepseek response: {}", response);
            // 检查响应是否有效
            ThrowUtils.throwIf(!response.startsWith("{"),
                    ErrorCode.INTERNAL_SERVER_ERROR, "Invalid JSON response: " + response);
            // 解析响应并提取内容
            AiChatResponse aiChatResponse = JSONUtil.toBean(response, AiChatResponse.class);
            String extractedResponse = extractResponse(aiChatResponse);
            // 增加用户今日消息计数
            incrementDailyMessageCount(userId);
            // 保存对话到 Redis 和 MySQL
            saveChatMessage(userId, query, extractedResponse);
            // 3.返回结果
            return extractedResponse;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating response for query: {}", query, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to generate response");
        }
    }

    /**
     * 构建 deepseek 请求对象
     * @param query 提问
     * @return 请求对象
     */
    private AiChatRequest buildRequest(String query) {
        return AiChatRequest.builder()
                .model(deepSeekConfig.getModel())
                .temperature(deepSeekConfig.getTemperature())
                .max_tokens(deepSeekConfig.getMaxTokens())
                .messages(Collections.singletonList(new AiChatRequest.Message("user", query)))
                .build();
    }

    /**
     * 提取回复内容
     * @param response 响应
     * @return 回复内容
     */
    private String extractResponse(AiChatResponse response) {
        return Optional.ofNullable(response)
                .map(AiChatResponse::getChoices)
                .filter(choices -> !choices.isEmpty())
                .map(choices -> choices.get(0))
                .map(choice -> choice.getMessage().getContent())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Empty response"));
    }

    /**
     * 增加用户今日消息计数
     * @param userId 用户ID
     */
    private void incrementDailyMessageCount(Long userId) {
        String today = java.time.LocalDate.now().toString();
        String countKey = String.format(DAILY_MESSAGE_COUNT_KEY, userId, today);
        // 增加计数
        stringRedisTemplate.opsForValue().increment(countKey);
        // 设置过期时间（如果key不存在）
        stringRedisTemplate.expire(countKey,
                java.time.Duration.between(
                        java.time.LocalDateTime.now(),
                        java.time.LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)
                )
        );
    }

    /**
     * 保存聊天记录
     * @param userId 用户ID
     * @param query 问题
     * @param response 回复
     */
    private void saveChatMessage(Long userId, String query, String response) {
        if (userId == null) {
            return;
        }
        String cacheKey = String.format(CHAT_CACHE_KEY, userId);
        try {
            // 保存用户的问题
            AiChat userMessage = new AiChat(userId, query, "user", new Date());
            String userMessageJson = JSONUtil.toJsonStr(userMessage);
            stringRedisTemplate.opsForList().rightPush(cacheKey, userMessageJson);
            // 保存AI的回复
            AiChat aiMessage = new AiChat(userId, response, "assistant", new Date());
            String aiMessageJson = JSONUtil.toJsonStr(aiMessage);
            stringRedisTemplate.opsForList().rightPush(cacheKey, aiMessageJson);
            // 检查是否需要同步到 MySQL
            syncChatMessagesIfNeeded(userId);
        } catch (Exception e) {
            log.error("Failed to save chat message", e);
        }
    }

    /**
     * 同步聊天记录到 MySQL
     * @param userId 用户ID
     */
    private void syncChatMessagesIfNeeded(Long userId) {
        String cacheKey = String.format(CHAT_CACHE_KEY, userId);
        Long messageCount = stringRedisTemplate.opsForList().size(cacheKey);
        final int MAX_MESSAGE_COUNT = 100;
        if (messageCount != null && messageCount >= MAX_MESSAGE_COUNT) {
            try {
                List<String> messages = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
                if (messages != null && !messages.isEmpty()) {
                    List<AiChat> chatMessages = messages.stream()
                            .map(msg -> JSONUtil.toBean(msg, AiChat.class))
                            .collect(Collectors.toList());
                    // 批量插入到 MySQL
                    aiChatMapper.batchInsert(chatMessages);
                    // 清空 Redis 中的消息
                    stringRedisTemplate.delete(cacheKey);
                }
            } catch (Exception e) {
                log.error("Failed to sync chat messages to MySQL", e);
            }
        }
    }

    /**
     * 获取聊天记录
     * @param request 请求
     * @param current 当前页
     * @param pageSize 每页条数
     * @return 聊天记录
     */
    @Override
    public Page<AiChatVO> getChatHistory(HttpServletRequest request, int current, int pageSize) {
        // 校验参数
        User loginUser = userService.isLogin(request);
        Long userId = loginUser.getId();
        if (userId == null) {
            return new Page<>();
        }
        try {
            // 检查并同步 Redis 数据到 MySQL
            syncRedisToMySql(userId);
            // 分页查询 MySQL 数据
            Page<AiChat> chatMessages = new Page<>(current, pageSize);
            QueryWrapper<AiChat> queryWrapper = new QueryWrapper<AiChat>()
                    .eq("userId", userId)
                    .orderByDesc("createTime");
            // todo Error attempting to get column 'role' from result set.
            // todo Cause: java.sql.SQLDataException: Cannot convert string 'user' to java.sql.Timestamp value
            // todo 暂时没检查出是哪里的问题，后续再看
            Page<AiChat> charMessagesPage = aiChatMapper.selectPage(chatMessages, queryWrapper);
            // 转换为 VO 并按对话对重新排序
            List<AiChatVO> chatMessageVOList = new ArrayList<>();
            List<AiChat> aiChatRecordList = charMessagesPage.getRecords();
            for (int i = 0; i < aiChatRecordList.size(); i += 2) {
                // 确保有成对的消息（用户提问 - AI 回答）
                if (i + 1 < aiChatRecordList.size()) {
                    AiChat userChatMessage = aiChatRecordList.get(i);
                    AiChat aiCharMessage = aiChatRecordList.get(i + 1);
                    // 确保 AI 的回答在前
                    if ("assistant".equals(aiCharMessage.getRole())) {
                        // 添加 AI 的回答
                        AiChatVO aiChatVo = new AiChatVO();
                        aiChatVo.setContent(aiCharMessage.getContent());
                        aiChatVo.setRole(aiCharMessage.getRole());
                        aiChatVo.setCreateTime(aiCharMessage.getCreateTime());
                        chatMessageVOList.add(aiChatVo);
                        // 添加用户的问题
                        AiChatVO userChatVo = new AiChatVO();
                        userChatVo.setContent(userChatMessage.getContent());
                        userChatVo.setRole(userChatMessage.getRole());
                        userChatVo.setCreateTime(userChatMessage.getCreateTime());
                        chatMessageVOList.add(userChatVo);
                    } else {
                        // 如果顺序不对，交换
                        AiChatVO userChatVo = new AiChatVO();
                        userChatVo.setContent(userChatMessage.getContent());
                        userChatVo.setRole(userChatMessage.getRole());
                        userChatVo.setCreateTime(userChatMessage.getCreateTime());
                        chatMessageVOList.add(userChatVo);
                        AiChatVO aiChatVo = new AiChatVO();
                        aiChatVo.setContent(aiCharMessage.getContent());
                        aiChatVo.setRole(aiCharMessage.getRole());
                        aiChatVo.setCreateTime(aiCharMessage.getCreateTime());
                        chatMessageVOList.add(aiChatVo);
                    }
                }
            }
            // 构建分页结果
            Page<AiChatVO> chatMessageVOPage = new Page<>(current, pageSize);
            chatMessageVOPage.setRecords(chatMessageVOList);
            chatMessageVOPage.setTotal(charMessagesPage.getTotal());
            return chatMessageVOPage;
        } catch (Exception e) {
            log.error("Failed to get chat history", e);
            return new Page<>();
        }
    }

    /**
     * 同步Redis数据到MySQL
     * @param userId 用户ID
     */
    private void syncRedisToMySql(Long userId) {
        String cacheKey = String.format(CHAT_CACHE_KEY, userId);
        List<String> messages = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (messages != null && !messages.isEmpty()) {
            try {
                // 转换消息
                List<AiChat> chatMessages = messages.stream()
                        .map(msg -> JSONUtil.toBean(msg, AiChat.class))
                        .collect(Collectors.toList());
                // 批量插入到 MySQL
                aiChatMapper.batchInsert(chatMessages);
                // 清空 Redis 中的消息
                stringRedisTemplate.delete(cacheKey);
                log.info("Successfully synced {} messages from Redis to MySQL for user {}", messages.size(), userId);
            } catch (Exception e) {
                log.error("Failed to sync Redis messages to MySQL for user {}", userId, e);
            }
        }
    }
}




