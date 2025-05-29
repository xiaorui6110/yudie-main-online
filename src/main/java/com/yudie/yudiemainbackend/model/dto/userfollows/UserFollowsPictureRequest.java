package com.yudie.yudiemainbackend.model.dto.userfollows;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 用户关注图片请求
 * @author: siri
 * @date: 2025-05-28 20:15
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFollowsPictureRequest extends PageRequest implements Serializable {

    /**
     * 是否只查询 spaceId 为 null 的数据
     */
    private boolean nullSpaceId;
    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
