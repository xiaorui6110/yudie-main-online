package com.yudie.yudiemainbackend.innerservice.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.User;

import java.util.List;

/**
 * @description:
 * @author: xiaorui
 * @date: 2026-02-05 20:33
 **/
public interface InnerUserMapper {

    List<User> selectList(QueryWrapper<User> queryWrapper);
}
