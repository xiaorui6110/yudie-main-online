package com.yudie.yudiemainbackend.model.dto.like;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 点赞查询请求
 * @author: xiaorui
 * @date: 2025-05-30 14:34
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class LikeQueryRequest extends PageRequest implements Serializable {

    /**
     * 目标类型：1-图片 2-帖子
     */
    private Integer targetType;

    private static final long serialVersionUID = 1L;
}
