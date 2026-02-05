package com.yudie.yudiemainbackend.controller;

import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 健康检查
 * @author: xiaorui
 * @date: 2025-05-20 15:04
 **/
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     * @return ok
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
