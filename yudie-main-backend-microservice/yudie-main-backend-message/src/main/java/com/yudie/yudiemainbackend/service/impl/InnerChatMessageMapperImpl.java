package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.innerservice.message.InnerChatMessageMapper;
import com.yudie.yudiemainbackend.mapper.ChatMessageMapper;
import com.yudie.yudiemainbackend.model.entity.ChatMessage;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerChatMessageMapperImpl implements InnerChatMessageMapper {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    public long selectCount(QueryWrapper<ChatMessage> queryWrapper) {
        return chatMessageMapper.selectCount(queryWrapper);
    }

    @Override
    public List<ChatMessage> selectList(QueryWrapper<ChatMessage> queryWrapper) {
        return chatMessageMapper.selectList(queryWrapper);
    }
}
