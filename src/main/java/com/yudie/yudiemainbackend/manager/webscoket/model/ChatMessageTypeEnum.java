package com.yudie.yudiemainbackend.manager.webscoket.model;

import lombok.Getter;

/**
 * @description: 聊天消息类型枚举
 * @author: siri
 * @date: 2025-05-31 15:03
 **/
@Getter
public enum ChatMessageTypeEnum {

    /**
     * 聊天消息类型枚举
     */
    CHAT("发送聊天消息", "CHAT"),
    JOIN("加入聊天", "JOIN"),
    LEAVE("离开聊天", "LEAVE"),
    HISTORY("历史消息", "HISTORY"),
    ONLINE("在线用户", "ONLINE"),
    ERROR("错误消息", "ERROR");

    private final String text;

    private final String value;

    ChatMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static ChatMessageTypeEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (ChatMessageTypeEnum typeEnum : ChatMessageTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
