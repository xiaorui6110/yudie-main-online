package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.yudie.yudiemainbackend.innerservice.post.InnerPostService;
import com.yudie.yudiemainbackend.mapper.PostMapper;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.yudie.yudiemainbackend.service.PostService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerPostServiceImpl implements InnerPostService {

    @Resource
    private PostService postService;

    @Override
    public Post getById(long postId) {
        return postService.getById(postId);
    }

    @Override
    public UpdateChainWrapper<Post> update() {
        return postService.update();
    }

    @Override
    public List<Post> list(QueryWrapper<Post> queryWrapper) {
        return postService.list(queryWrapper);
    }

    @Override
    public boolean remove(QueryWrapper<Post> queryWrapper) {
        return postService.remove(queryWrapper);
    }

    @Override
    public void fillPostInfo(Post post) {
        postService.fillPostInfo(post);
    }

    @Override
    public List<Post> getTop100Post(Long type) {
        return postService.getTop100Post(type);
    }
}
