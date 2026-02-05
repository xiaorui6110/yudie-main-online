package com.yudie.yudiemainbackend.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @description: 请求频率限制
 * @author: xiaorui
 * @date: 2025-07-11 09:35
 **/
@Component
public class RateLimiter {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMITER_PREFIX = "rate_limiter:";

    private static final String MESSAGE_ADD_PREFIX = "message_add:";

    private static final String MESSAGE_QUERY_PREFIX = "message_query:";

    /**
     * 检查是否允许添加留言
     * @param ip 用户IP
     * @return 是否允许
     */
    public boolean allowMessageAdd(String ip) {
        String key = RATE_LIMITER_PREFIX + MESSAGE_ADD_PREFIX + ip;
        // 每60秒最多5次
        return checkRate(key, 5, 60);
    }

    /**
     * 检查是否允许查询留言
     * @param ip 用户IP
     * @return 是否允许
     */
    public boolean allowMessageQuery(String ip) {
        String key = RATE_LIMITER_PREFIX + MESSAGE_QUERY_PREFIX + ip;
        // 每60秒最多30次
        return checkRate(key, 30, 60);
    }

    /**
     * 检查访问频率
     * @param key Redis key
     * @param limit 限制次数
     * @param seconds 时间窗口（秒）
     * @return 是否允许访问
     */
    private boolean checkRate(String key, int limit, int seconds) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count != null && count == 1) {
            redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        }
        return count != null && count <= limit;
    }
}
