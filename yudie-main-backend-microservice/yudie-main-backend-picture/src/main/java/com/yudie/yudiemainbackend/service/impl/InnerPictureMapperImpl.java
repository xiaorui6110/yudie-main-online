package com.yudie.yudiemainbackend.service.impl;

import com.yudie.yudiemainbackend.innerservice.picture.InnerPictureMapper;
import com.yudie.yudiemainbackend.mapper.PictureMapper;
import com.yudie.yudiemainbackend.model.entity.Picture;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class InnerPictureMapperImpl implements InnerPictureMapper {

    @Resource
    private PictureMapper pictureMapper;

    @Override
    public Picture selectById(Long id) {
        return pictureMapper.selectById(id);
    }
}
