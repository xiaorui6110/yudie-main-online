package com.yudie.yudiemainbackend.innerservice.post;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.Post;

import java.util.List;

public interface InnerPostMapper {

    List<Post> selectList(QueryWrapper<Post> queryWrapper);
}
