package com.yudie.yudiemainbackend.innerservice.post;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.yudie.yudiemainbackend.model.entity.Post;

import java.util.List;

public interface InnerPostService {

    Post getById(long postId);

    UpdateChainWrapper<Post> update();

    List<Post> list(QueryWrapper<Post> queryWrapper);

    boolean remove(QueryWrapper<Post> queryWrapper);

    void fillPostInfo(Post post);

    List<Post> getTop100Post(Long type);
}
