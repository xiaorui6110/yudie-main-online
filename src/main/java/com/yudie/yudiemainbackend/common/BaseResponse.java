package com.yudie.yudiemainbackend.common;

import com.yudie.yudiemainbackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 全局响应封装类
 * @author: xiaorui
 * @date: 2025-05-20 16:43
 **/
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;


    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
