package com.yudie.yudiemainbackend.innerservice.extend;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.ShareRecord;

import java.util.List;

public interface InnerShareRecordService {

    boolean isContentShared(Long targetId, Integer targetType, Long userId);

    long getUnreadSharesCount(Long userId);

    void clearAllUnreadShares(Long userId);

    List<ShareRecord> list(QueryWrapper<ShareRecord> queryWrapper);
}
