package com.yudie.yudiemainbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description: Ai DeepSeek 配置
 * @author: xiaorui
 * @date: 2025-07-13 10:37
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.deepseek")
public class DeepSeekConfig {

    /**
     * AI API KEY
     */
    private String apiKey;

    /**
     * AI API URL
     */
    private String apiUrl;

    /**
     * AI 模型ID
     */
    private String model;

    /**
     * AI 采样温度
     */
    private Double temperature;

    /**
     * AI 模型最大token数
     */
    private Integer maxTokens;


}
