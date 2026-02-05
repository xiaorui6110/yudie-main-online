package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.yudie.yudiemainbackend.innerservice.space.InnerSpaceService;
import com.yudie.yudiemainbackend.model.entity.Space;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import com.yudie.yudiemainbackend.service.SpaceService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;

@DubboService
public class InnerSpaceServiceImpl implements InnerSpaceService {

    @Resource
    private SpaceService spaceService;

    @Override
    public Space getById(Serializable id) {
        return spaceService.getById(id);
    }

    @Override
    public LambdaUpdateChainWrapper<Space> lambdaUpdate() {
        return spaceService.lambdaUpdate();
    }

    @Override
    public LambdaQueryChainWrapper<Space> lambdaQuery() {
        return spaceService.lambdaQuery();
    }

    @Override
    public SpaceVO getSpaceVOById(long id, HttpServletRequest request) {
        return spaceService.getSpaceVOById(id, request);
    }
}
