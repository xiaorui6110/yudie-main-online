package com.yudie.yudiemainbackend.manager.webscoket.disruptor;

import com.yudie.yudiemainbackend.manager.webscoket.model.PictureEditRequestMessage;
import com.yudie.yudiemainbackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @description: 图片编辑事件
 * @author: siri
 * @date: 2025-05-28 19:52
 **/
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}
