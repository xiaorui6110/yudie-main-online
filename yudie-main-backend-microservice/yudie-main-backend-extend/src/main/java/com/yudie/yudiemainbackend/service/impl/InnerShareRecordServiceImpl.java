package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.extend.InnerShareRecordService;
import com.yudie.yudiemainbackend.model.entity.ShareRecord;
import com.yudie.yudiemainbackend.service.ShareRecordService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerShareRecordServiceImpl implements InnerShareRecordService {

    @Resource
    private ShareRecordService shareRecordService;

    @Override
    public boolean isContentShared(Long targetId, Integer targetType, Long userId) {
        return shareRecordService.isContentShared(targetId, targetType, userId);
    }

    @Override
    public long getUnreadSharesCount(Long userId) {
        return shareRecordService.getUnreadSharesCount(userId);
    }

    @Override
    public void clearAllUnreadShares(Long userId) {
        shareRecordService.clearAllUnreadShares(userId);
    }

    @Override
    public List<ShareRecord> list(QueryWrapper<ShareRecord> queryWrapper) {
        return shareRecordService.list(queryWrapper);
    }
}
