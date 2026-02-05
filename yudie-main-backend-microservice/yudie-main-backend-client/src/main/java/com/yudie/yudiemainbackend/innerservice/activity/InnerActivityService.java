package com.yudie.yudiemainbackend.innerservice.activity;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yudie.yudiemainbackend.model.entity.Activity;

public interface InnerActivityService {

    boolean update( UpdateWrapper<Activity> updateWrapper);
}
