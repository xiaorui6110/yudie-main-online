package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.model.dto.message.AddMessage;
import com.yudie.yudiemainbackend.model.entity.Message;
import com.yudie.yudiemainbackend.model.vo.MessageVO;
import com.yudie.yudiemainbackend.service.MessageService;
import com.yudie.yudiemainbackend.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【message(留言板表)】的数据库操作Service实现
* @createDate 2025-05-31 10:38:11
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

    /**
     * 添加留言
     * @param addMessage 添加留言请求
     * @return 添加留言结果
     */
    @Override
    public Boolean addMessage(AddMessage addMessage) {
        Message message = new Message();
        message.setContent(addMessage.getContent());
        message.setIp(addMessage.getIp());
        return this.save(message);
    }

    /**
     * 获取热门留言列表
     * @return 留言列表
     */
    @Override
    public List<MessageVO> getTop500() {
        return this.baseMapper.getTop500();
    }
}
