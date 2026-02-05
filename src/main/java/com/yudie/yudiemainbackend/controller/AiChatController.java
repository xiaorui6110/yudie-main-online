package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.vo.AiChatVO;
import com.yudie.yudiemainbackend.service.AiChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @description: Ai 聊天实现类接口
 * @author: xiaorui
 * @date: 2025-07-13 11:37
 **/
@RestController
@RequestMapping("/deepseek")
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    /**
     * 发送消息
     * @param query 问题
     * @param request 请求
     * @return 回复
     */
    @PostMapping("/send")
    public String send(String query, HttpServletRequest request) {
        return aiChatService.generateResponse(query, request);
    }

    /**
     * 获取历史记录
     * @param current 当前页
     * @param pageSize 每页条数
     * @param request 请求
     * @return 历史记录
     */
    @PostMapping("/history")
    public Page<AiChatVO> getChatHistory(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        return aiChatService.getChatHistory(request, current, pageSize);
    }


}
