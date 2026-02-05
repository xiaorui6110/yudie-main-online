package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.post.InnerPostAttachmentService;
import com.yudie.yudiemainbackend.mapper.PostAttachmentMapper;
import com.yudie.yudiemainbackend.model.entity.PostAttachment;
import com.yudie.yudiemainbackend.service.PostAttachmentService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerPostAttachmentServiceImpl implements InnerPostAttachmentService {

    @Resource
    private PostAttachmentService postAttachmentService;

    @Override
    public boolean saveBatch(List<PostAttachment> postAttachments) {
        return postAttachmentService.saveBatch(postAttachments);
    }

    @Override
    public List<PostAttachment> list(QueryWrapper<PostAttachment> queryWrapper) {
        return postAttachmentService.list(queryWrapper);
    }

    @Override
    public boolean remove(QueryWrapper<PostAttachment> queryWrapper) {
        return postAttachmentService.remove(queryWrapper);
    }
}
