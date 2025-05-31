package com.yudie.yudiemainbackend.manager.webscoket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * @description: WebSocket 配置（定义连接）
 * @author: siri
 * @date: 2025-05-28 19:51
 **/
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private WsHandshakeInterceptor wsHandshakeInterceptor;

    @Resource
    private ChatWebSocketServer chatWebSocketServer;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pictureEditHandler, "/ws/picture/edit")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins("*");

        // 添加聊天WebSocket配置
        registry.addHandler(chatWebSocketServer, "/ws/chat")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}