package com.yudie.yudiemainbackend.service;

import com.yudie.yudiemainbackend.model.dto.message.AddMessage;
import com.yudie.yudiemainbackend.model.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.vo.MessageVO;

import java.util.List;

/**
* @author lenovo
* @description 针对表【message(留言板表)】的数据库操作Service
* @createDate 2025-05-31 10:38:11
*/
public interface MessageService extends IService<Message> {

    /**
     * 添加留言
     * @param addMessage 添加留言请求
     * @return 添加留言结果
     */
    Boolean addMessage(AddMessage addMessage);

    /**
     * 获取热门留言列表
     * @return 留言列表
     */
    List<MessageVO> getTop500();
}
