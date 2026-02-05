package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.dto.message.MessageAddRequest;
import com.yudie.yudiemainbackend.model.dto.message.MessageQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Message;
import com.yudie.yudiemainbackend.model.vo.MessageVO;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【message(留言板表)】的数据库操作Service
* @createDate 2025-05-31 10:38:11
*/
public interface MessageService extends IService<Message> {

    /**
     * 添加留言
     * @param messageAddRequest 添加留言请求
     * @return 添加留言结果
     */
    Boolean addMessage(MessageAddRequest messageAddRequest);

    /**
     * 获取热门留言列表
     * @return 留言列表
     */
    List<MessageVO> getTop500();

    /**
     * 分页获取留言列表
     * @param messageQueryRequest 查询条件
     * @return 留言列表
     */
    Page<Message> getMessagePage(MessageQueryRequest messageQueryRequest);

    /**
     * 获取查询条件
     * @param messageQueryRequest 查询留言请求
     * @return 查询条件
     */
    QueryWrapper<Message> getQueryWrapper(MessageQueryRequest messageQueryRequest);

    /**
     * 删除留言
     * @param id 留言id
     * @return 是否成功
     */
    boolean deleteMessage(long id);
}
