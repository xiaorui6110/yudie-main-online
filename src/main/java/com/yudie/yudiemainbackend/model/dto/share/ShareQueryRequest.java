package com.yudie.yudiemainbackend.model.dto.share;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 分享查询请求
 * @author: siri
 * @date: 2025-05-30 14:35
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ShareQueryRequest extends PageRequest implements Serializable {

    /**
     * 目标类型：1-图片 2-帖子 3-空间
     */
    private Integer targetType;

    private static final long serialVersionUID = 1L;

}