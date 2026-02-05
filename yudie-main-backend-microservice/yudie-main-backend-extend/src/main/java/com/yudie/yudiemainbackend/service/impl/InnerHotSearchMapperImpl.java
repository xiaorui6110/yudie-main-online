package com.yudie.yudiemainbackend.service.impl;

import com.yudie.yudiemainbackend.innerservice.extend.InnerHotSearchMapper;
import com.yudie.yudiemainbackend.mapper.HotSearchMapper;
import com.yudie.yudiemainbackend.model.entity.HotSearch;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: xiaorui
 * @date: 2026-02-05 20:29
 **/
@DubboService
public class InnerHotSearchMapperImpl implements InnerHotSearchMapper {

    @Resource
    private HotSearchMapper hotSearchMapper;

    @Override
    public List<HotSearch> getHotSearchAfter(String type, Date startTime, Integer limit) {
        return hotSearchMapper.getHotSearchAfter(type, startTime, limit);
    }
}
