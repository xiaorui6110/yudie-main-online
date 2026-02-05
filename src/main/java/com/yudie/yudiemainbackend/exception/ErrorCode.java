package com.yudie.yudiemainbackend.exception;

import lombok.Getter;

/**
 * @description: 错误码枚举类
 * @author: xiaorui
 * @date: 2025-05-20 16:26
 **/
@Getter
public enum ErrorCode {

    /**
     * 通用错误码
     */
    SUCCESS(0, "成功"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NOT_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求资源不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    TOO_MANY_REQUESTS_ERROR(42900, "请求过于频繁"),
    INTERNAL_SERVER_ERROR(50000, "服务器内部错误"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
