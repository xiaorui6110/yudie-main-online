package com.yudie.yudiemainbackend.model.dto.privatechat;

import com.yudie.yudiemainbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 私聊查询请求
 * @author: xiaorui
 * @date: 2025-05-31 15:09
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class PrivateChatQueryRequest extends PageRequest implements Serializable {

    /**
     * 目标用户id
     */
    private Long targetUserId;

    /**
     * 聊天类型：0-私信 1-好友
     */
    private Integer chatType;

    /**
     * 搜索词（搜索最后一条消息内容）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}
