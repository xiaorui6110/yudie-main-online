package com.yudie.yudiemainbackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description: 空间级别
 * @author: siri
 * @date: 2025-05-27 14:49
 **/
@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 值
     */
    private int value;

    /**
     * 中文
     */
    private String text;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;
}
