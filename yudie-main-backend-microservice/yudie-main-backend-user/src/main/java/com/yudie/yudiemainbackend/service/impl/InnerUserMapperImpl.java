package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserMapper;
import com.yudie.yudiemainbackend.mapper.UserMapper;
import com.yudie.yudiemainbackend.model.entity.User;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;


@DubboService
public class InnerUserMapperImpl implements InnerUserMapper {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> selectList(QueryWrapper<User> queryWrapper) {
        return userMapper.selectList(queryWrapper);
    }
}
