package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.model.dto.message.MessageAddRequest;
import com.yudie.yudiemainbackend.model.dto.message.MessageQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Message;
import com.yudie.yudiemainbackend.model.vo.MessageVO;
import com.yudie.yudiemainbackend.service.MessageService;
import com.yudie.yudiemainbackend.utils.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 留言实现类接口
 * @author: xiaorui
 * @date: 2025-05-31 10:48
 **/
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private RateLimiter rateLimiter;

    /**
     * 添加留言
     * @param messageAddRequest 添加留言请求
     * @param request 请求
     * @return BaseResponse
     */
    @PostMapping("/add")
    @SuppressWarnings("unchecked")
    public BaseResponse<Boolean> addMessage(@RequestBody MessageAddRequest messageAddRequest, HttpServletRequest request) {
        // 获取真实IP地址
        String ip = getIpAddress(request);
        if (!rateLimiter.allowMessageAdd(ip)) {
            return (BaseResponse<Boolean>) ResultUtils.error(ErrorCode.TOO_MANY_REQUESTS_ERROR, "请求过于频繁,请稍后再试");
        }
        messageAddRequest.setIp(ip);
        return ResultUtils.success(messageService.addMessage(messageAddRequest));
    }

    /**
     * 获取留言板前500条留言
     * @param request 请求
     * @return BaseResponse
     */
    @PostMapping("/getTop500")
    @SuppressWarnings("unchecked")
    public BaseResponse<List<MessageVO>> getTop500(HttpServletRequest request) {
        String ip = getIpAddress(request);
        if (!rateLimiter.allowMessageQuery(ip)) {
            return (BaseResponse<List<MessageVO>>) ResultUtils.error(ErrorCode.TOO_MANY_REQUESTS_ERROR, "请求过于频繁,请稍后再试");
        }
        return ResultUtils.success(messageService.getTop500());
    }

    /**
     * 分页获取留言列表
     * @param messageQueryRequest 查询留言请求
     * @param request 请求
     * @return 留言列表
     */
    @PostMapping("/list/page")
    @SuppressWarnings("unchecked")
    public BaseResponse<Page<Message>> listMessageByPage(@RequestBody MessageQueryRequest messageQueryRequest,
                                                         HttpServletRequest request) {
        String ip = getIpAddress(request);
        if (!rateLimiter.allowMessageQuery(ip)) {
            return (BaseResponse<Page<Message>>) ResultUtils.error(ErrorCode.TOO_MANY_REQUESTS_ERROR, "查询太频繁，请稍后再试");
        }
        return ResultUtils.success(messageService.getMessagePage(messageQueryRequest));
    }

    /**
     * 删除留言
     * @param id 留言ID
     * @return 是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMessage(@RequestParam("id") long id) {
        return ResultUtils.success(messageService.deleteMessage(id));
    }

    /**
     * 获取真实IP地址
     * @param request 请求
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 处理本地 IPv6 地址
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ip = "127.0.0.1";
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

}
