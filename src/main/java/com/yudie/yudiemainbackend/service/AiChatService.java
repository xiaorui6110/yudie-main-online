package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.AiChat;
import com.yudie.yudiemainbackend.model.vo.AiChatVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author xiaorui
* @description 针对表【ai_chat(聊天消息表)】的数据库操作Service
* @createDate 2025-07-13 10:24:34
*/
public interface AiChatService extends IService<AiChat> {

    /**
     * 生成回复
     * @param query 提问
     * @param request 请求
     * @return 回复
     */
    String generateResponse(String query, HttpServletRequest request);

    /**
     * 获取聊天记录
     * @param request 请求
     * @param current 当前页
     * @param pageSize 每页条数
     * @return 聊天记录
     */
    Page<AiChatVO> getChatHistory(HttpServletRequest request, int current, int pageSize);

}
