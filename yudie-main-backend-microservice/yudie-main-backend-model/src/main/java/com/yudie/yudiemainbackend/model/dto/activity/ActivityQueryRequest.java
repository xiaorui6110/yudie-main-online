package com.yudie.yudiemainbackend.model.dto.activity;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 查询活动请求
 * @author: xiaorui
 * @date: 2025-07-10 20:56
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否只看未过期
     */
    private Boolean notExpired;

    /**
     * 是否公开查询（不需要管理员权限）
     */
    private Boolean isPublic;

    private static final long serialVersionUID = 1L;
}
