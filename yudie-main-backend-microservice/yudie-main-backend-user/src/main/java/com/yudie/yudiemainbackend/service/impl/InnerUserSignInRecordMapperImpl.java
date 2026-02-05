package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserSignInRecordMapper;
import com.yudie.yudiemainbackend.mapper.UserSignInRecordMapper;
import com.yudie.yudiemainbackend.model.entity.UserSignInRecord;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


@DubboService
public class InnerUserSignInRecordMapperImpl implements InnerUserSignInRecordMapper {

    @Resource
    private UserSignInRecordMapper userSignInRecordMapper;

    @Override
    public UserSignInRecord selectOne(QueryWrapper<UserSignInRecord> queryWrapper) {
        return userSignInRecordMapper.selectOne(queryWrapper);
    }

    @Override
    public int insert(UserSignInRecord userSignInRecord) {
        return userSignInRecordMapper.insert(userSignInRecord);
    }

    @Override
    public boolean updateById(UserSignInRecord userSignInRecord) {
        return userSignInRecordMapper.updateById(userSignInRecord) > 0;
    }
}
