package com.yudie.yudiemainbackend.controller;

import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.model.dto.message.AddMessage;
import com.yudie.yudiemainbackend.model.vo.MessageVO;
import com.yudie.yudiemainbackend.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 留言实现类接口
 * @author: siri
 * @date: 2025-05-31 10:48
 **/
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 添加留言
     * @param addMessage 添加留言请求
     * @param request 请求
     * @return BaseResponse
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addMessage(@RequestBody AddMessage addMessage, HttpServletRequest request) {
        String ip = getIpAddress(request);
        addMessage.setIp(ip);
        return ResultUtils.success(messageService.addMessage(addMessage));
    }

    /**
     * 获取留言板前500条
     * @return BaseResponse
     */
    @PostMapping("/getTop500")
    public BaseResponse<List<MessageVO>> getTop500() {
        return ResultUtils.success(messageService.getTop500());
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
