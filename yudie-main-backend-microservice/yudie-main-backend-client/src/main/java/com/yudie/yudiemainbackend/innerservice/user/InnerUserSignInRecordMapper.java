package com.yudie.yudiemainbackend.innerservice.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.UserSignInRecord;

public interface InnerUserSignInRecordMapper {

    UserSignInRecord selectOne(QueryWrapper<UserSignInRecord> queryWrapper);

    int insert(UserSignInRecord userSignInRecord);

    boolean updateById(UserSignInRecord userSignInRecord);
}
