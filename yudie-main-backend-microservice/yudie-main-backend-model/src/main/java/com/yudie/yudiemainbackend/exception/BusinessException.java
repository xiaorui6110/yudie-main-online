package com.yudie.yudiemainbackend.exception;

import lombok.Getter;

/**
 * @description: 业务异常类
 * @author: xiaorui
 * @date: 2025-05-20 16:35
 **/
@Getter
public class BusinessException extends RuntimeException{

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
