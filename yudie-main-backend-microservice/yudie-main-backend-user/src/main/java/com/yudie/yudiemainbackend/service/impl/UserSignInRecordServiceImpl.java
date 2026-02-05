package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.mapper.UserSignInRecordMapper;
import com.yudie.yudiemainbackend.model.entity.UserSignInRecord;
import com.yudie.yudiemainbackend.service.UserSignInRecordService;
import org.springframework.stereotype.Service;

/**
* @author xiaorui
* @description 针对表【user_sign_in_record(用户签到记录表)】的数据库操作Service实现
* @createDate 2025-05-23 12:15:25
*/
@Service
public class UserSignInRecordServiceImpl extends ServiceImpl<UserSignInRecordMapper, UserSignInRecord>
    implements UserSignInRecordService{

}




