package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yudie.yudiemainbackend.innerservice.activity.InnerActivityService;
import com.yudie.yudiemainbackend.model.entity.Activity;
import com.yudie.yudiemainbackend.service.ActivityService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class InnerActivityServiceImpl implements InnerActivityService {

    @Resource
    private ActivityService activityService;

    @Override
    public boolean update(UpdateWrapper<Activity> updateWrapper) {
        return activityService.update(updateWrapper);
    }
}
