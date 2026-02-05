package com.yudie.yudiemainbackend.innerservice.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.Userfollows;

import java.io.Serializable;
import java.util.List;

public interface InnerUserfollowsService {

    List<Long> getFollowList(Serializable id);

    List<Userfollows> list(QueryWrapper<Userfollows> queryWrapper);

    long count(QueryWrapper<Userfollows> queryWrapper);

}
