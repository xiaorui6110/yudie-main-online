package com.yudie.yudiemainbackend.innerservice.message;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;

import java.util.List;

public interface InnerChatMessageMapper {

    long selectCount(QueryWrapper<ChatMessage> queryWrapper);

    List<ChatMessage> selectList(QueryWrapper<ChatMessage> queryWrapper);

}
