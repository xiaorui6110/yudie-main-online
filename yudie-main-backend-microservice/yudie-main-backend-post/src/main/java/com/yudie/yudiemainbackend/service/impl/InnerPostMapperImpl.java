package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.post.InnerPostMapper;
import com.yudie.yudiemainbackend.mapper.PostMapper;
import com.yudie.yudiemainbackend.model.entity.Post;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerPostMapperImpl implements InnerPostMapper {

    @Resource
    private PostMapper postMapper;

    @Override
    public List<Post> selectList(QueryWrapper<Post> queryWrapper) {
        return postMapper.selectList(queryWrapper);
    }
}
