package com.yudie.yudiemainbackend.job;

import cn.hutool.core.util.RandomUtil;
import com.yudie.yudiemainbackend.mapper.HotSearchMapper;
import com.yudie.yudiemainbackend.model.entity.HotSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: 热门搜索缓存预热任务
 * @author: xiaorui
 * @date: 2025-05-31 16:17
 **/
@Slf4j
@Component
public class HotSearchSyncJob implements HotSearchSync, CommandLineRunner {

    private static final String HOT_SEARCH_CACHE_KEY = "hot_search:%s";

    private static final int DEFAULT_SIZE = 50;

    private static final String[] SEARCH_TYPES = {"picture", "user", "post", "space"};

    @Resource
    private HotSearchMapper hotSearchMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 应用启动时进行缓存预热
     */
    @Override
    @Async
    public void run(String... args) {
        try {
            log.info("开始热门搜索缓存预热");
            // 延迟5秒，等待其他组件初始化完成
            TimeUnit.SECONDS.sleep(5);
            warmUpCache();
            log.info("热门搜索缓存预热完成");
        } catch (Exception e) {
            log.error("热门搜索缓存预热失败", e);
        }
    }

    /**
     * 缓存预热
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void warmUpCache() {
        // 获取最近24小时的数据
        Date startTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

        for (String type : SEARCH_TYPES) {
            try {
                // 先尝试从MySQL获取数据
                List<HotSearch> hotSearchList = hotSearchMapper.getHotSearchAfter(type, startTime, DEFAULT_SIZE);
                // 更新Redis缓存
                if (!hotSearchList.isEmpty()) {
                    updateCache(type, hotSearchList);
                    log.info("类型{}的热门搜索缓存预热成功，数量: {}", type, hotSearchList.size());
                }
            } catch (Exception e) {
                log.error("类型{}的热门搜索缓存预热失败", type, e);
            }
        }
    }


    /**
     * 每2小时更新Redis缓存
     */
    @Override
    public void updateCache(String type, List<HotSearch> hotSearchList) {
        String cacheKey = String.format(HOT_SEARCH_CACHE_KEY, type);
        try {
            // 获取前50个热门搜索词
            List<String> keywords = hotSearchList.stream()
                    .limit(DEFAULT_SIZE)
                    .map(HotSearch::getKeyword)
                    .collect(Collectors.toList());

            // 使用管道批量操作
            stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
                connection.multi();
                // 删除旧的缓存
                connection.del(cacheKey.getBytes());
                // 添加新的缓存
                for (String keyword : keywords) {
                    connection.rPush(cacheKey.getBytes(), keyword.getBytes());
                }
                // 设置过期时间（13-15分钟随机）
                long expireSeconds = 13 * 60 + RandomUtil.randomInt(0, 120);
                connection.expire(cacheKey.getBytes(), expireSeconds);
                connection.exec();
                return null;
            });
        } catch (Exception e) {
            log.error("更新热门搜索缓存失败, type={}", type, e);
        }
    }
}
