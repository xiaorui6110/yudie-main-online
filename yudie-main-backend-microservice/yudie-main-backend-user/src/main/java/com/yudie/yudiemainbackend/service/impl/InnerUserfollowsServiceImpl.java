package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserfollowsService;
import com.yudie.yudiemainbackend.model.entity.Userfollows;
import com.yudie.yudiemainbackend.service.UserfollowsService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class InnerUserfollowsServiceImpl implements InnerUserfollowsService {

    @Resource
    private UserfollowsService userfollowsService;

    @Override
    public List<Long> getFollowList(Serializable id) {
        if (id== null) {
            return null;
        }
        QueryWrapper<Userfollows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", id);
        queryWrapper.eq("followStatus", 1);
        List<Userfollows> userfollowsList = this.list(queryWrapper);
        return userfollowsList.stream().map(Userfollows::getFollowingId).collect(Collectors.toList());
    }

    @Override
    public List<Userfollows> list(QueryWrapper<Userfollows> queryWrapper) {
        return userfollowsService.list(queryWrapper);
    }

    @Override
    public long count(QueryWrapper<Userfollows> queryWrapper) {
        return userfollowsService.count(queryWrapper);
    }
}
