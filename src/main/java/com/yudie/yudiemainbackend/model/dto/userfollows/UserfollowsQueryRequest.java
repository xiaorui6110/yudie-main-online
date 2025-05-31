package com.yudie.yudiemainbackend.model.dto.userfollows;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 用户关注查询请求
 * @author: xiaorui
 * @date: 2025-05-28 20:14
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserfollowsQueryRequest extends PageRequest implements Serializable {

    /**
     * 关注者的用户 ID
     */
    private Long followerId;
    /**
     * 被关注者的用户 ID
     */
    private Long followingId;
    /**
     * 搜索类型,0为关注，1为粉丝
     */
    private Integer searchType;

    private static final long serialVersionUID = 3L;
}
