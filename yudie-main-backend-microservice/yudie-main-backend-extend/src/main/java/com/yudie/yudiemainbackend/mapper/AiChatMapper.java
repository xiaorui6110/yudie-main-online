package com.yudie.yudiemainbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yudie.yudiemainbackend.model.entity.AiChat;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【ai_chat(聊天消息表)】的数据库操作Mapper
* @createDate 2025-07-13 10:24:34
* @Entity com.yudie.yudiemainbackend.model.entity.AiChat
*/
public interface AiChatMapper extends BaseMapper<AiChat> {

    /**
     * 批量插入
     * @param chatMessages 聊天消息
     */
    void batchInsert(List<AiChat> chatMessages);

}




