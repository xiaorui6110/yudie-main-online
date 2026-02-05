package com.yudie.yudiemainbackend.model.dto.aichat;

import lombok.Data;

import java.util.List;

/**
 * @description: AI 聊天返回信息
 * @author: xiaorui
 * @date: 2025-07-13 11:07
 * DeepSeek API 官网：<a href="https://api-docs.deepseek.com/zh-cn/api/create-chat-completion">...</a>
 *
 **/
@Data
public class AiChatResponse {

    /**
     * 对话的唯一标识符
     */
    private String id;

    /**
     * 模型生成的 completion 的选择列表
     */
    private List<Choices> choices;

    /**
     * 创建聊天完成时的 Unix 时间戳（以秒为单位）
     */
    private Integer created;

    /**
     * 生成该 completion 的模型名
     */
    private String model;

    /**
     * 对象的类型
     */
    private String object;

    /**
     * 对话补全请求的用量信息
     */
    private Usage usage;

    /**
     * 此指纹表示模型运行时使用的后端配置
     * 主要是 DeepSeek 那边的服务器接收的是下划线，最好不修改
     * 故以下均是下划线命名
     */
    private String system_fingerprint;

    @Data
    public static class Choices {

        private String finish_reason;

        private Integer index;

        private Message message;

        /**
         * NullAble 可以为 null，故不多补充了
         */
        private String logprobs;

        @Data
        public static class Message {

            private String role;

            private String content;
        }
    }

    @Data
    public static class Usage {

        private Integer completion_tokens;

        private Integer prompt_tokens;

        private Integer prompt_cache_hit_tokens;

        private Integer prompt_cache_miss_tokens;

        private Integer total_tokens;

        private Prompt_tokens_details prompt_tokens_details;

        @Data
        public static class Prompt_tokens_details {

            private Integer cached_tokens;

        }
    }

}
