package com.yudie.yudiemainbackend.job;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yudie.yudiemainbackend.model.entity.Activity;
import com.yudie.yudiemainbackend.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description: 活动过期检查定时任务
 * @author: xiaorui
 * @date: 2025-07-10 22:22
 **/
@Slf4j
@Component
public class ActivityExpirationJob {

    @Resource
    private ActivityService activityService;

    /**
     * 每天凌晨1点执行过期检查
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredActivities() {
        try {
            log.info("开始检查过期活动");
            // 更新所有已过期但未标记的活动
            UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
            // 未标记过期的
            updateWrapper.eq("isExpired", 0)
                    // 过期时间早于当前时间
                    .lt("expireTime", new Date())
                    // 标记为已过期
                    .set("isExpired", 1);
            boolean success = activityService.update(updateWrapper);
            log.info("过期活动检查完成，更新状态：{}", success);
        } catch (Exception e) {
            log.error("过期活动检查任务失败", e);
        }
    }
}
