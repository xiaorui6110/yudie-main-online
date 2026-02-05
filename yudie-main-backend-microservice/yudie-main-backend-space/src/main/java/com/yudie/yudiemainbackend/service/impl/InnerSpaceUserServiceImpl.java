package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yudie.yudiemainbackend.innerservice.space.InnerSpaceUserService;
import com.yudie.yudiemainbackend.model.entity.SpaceUser;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.SpaceUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerSpaceUserServiceImpl implements InnerSpaceUserService {

    @Resource
    private SpaceUserService spaceUserService;

    @Override
    public boolean isSpaceMember(long userId, long spaceId) {
        return spaceUserService.isSpaceMember(userId, spaceId);
    }

    @Override
    public List<User> getSpaceMembers(long spaceId) {
        return spaceUserService.getSpaceMembers(spaceId);
    }

    @Override
    public LambdaQueryChainWrapper<SpaceUser> lambdaQuery() {
        return spaceUserService.lambdaQuery();
    }

    @Override
    public SpaceUser getById(long id) {
        return spaceUserService.getById(id);
    }
}
