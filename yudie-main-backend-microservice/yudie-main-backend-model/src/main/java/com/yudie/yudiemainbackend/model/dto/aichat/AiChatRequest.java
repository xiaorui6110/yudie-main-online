package com.yudie.yudiemainbackend.model.dto.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description: AI 聊天请求
 * @author: xiaorui
 * @date: 2025-07-13 10:41
 * DeepSeek API 官网：<a href="https://api-docs.deepseek.com/zh-cn/api/create-chat-completion">...</a>
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    /**
     * 对话的消息列表
     */
    private List<Message> messages;

    /**
     * 使用的模型的 ID
     */
    private String model;

    /**
     * 采样温度，介于 0 和 2 之间（默认为1）
     */
    private Double temperature;

    /**
     * 最大生成长度
     */
    private Integer max_tokens;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private String role;

        private String content;
    }

}

