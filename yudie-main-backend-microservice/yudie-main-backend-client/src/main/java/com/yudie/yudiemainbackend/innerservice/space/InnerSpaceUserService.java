package com.yudie.yudiemainbackend.innerservice.space;


import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yudie.yudiemainbackend.model.entity.SpaceUser;
import com.yudie.yudiemainbackend.model.entity.User;

import java.util.List;

public interface InnerSpaceUserService {

    boolean isSpaceMember(long userId, long spaceId);

    List<User> getSpaceMembers(long spaceId);

    LambdaQueryChainWrapper<SpaceUser> lambdaQuery();

    SpaceUser getById(long id);

}
