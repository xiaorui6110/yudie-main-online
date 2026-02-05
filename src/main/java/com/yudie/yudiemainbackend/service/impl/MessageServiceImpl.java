package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.dto.message.MessageAddRequest;
import com.yudie.yudiemainbackend.model.dto.message.MessageQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Message;
import com.yudie.yudiemainbackend.model.vo.MessageVO;
import com.yudie.yudiemainbackend.service.MessageService;
import com.yudie.yudiemainbackend.mapper.MessageMapper;
import com.yudie.yudiemainbackend.utils.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author xiaorui
* @description 针对表【message(留言板表)】的数据库操作Service实现
* @createDate 2025-05-31 10:38:11
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

    @Resource
    private RateLimiter rateLimiter;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String MESSAGE_CACHE_KEY = "message:top500";

    private static final long CACHE_EXPIRE_TIME = 5;

    /**
     * 添加留言
     * @param messageAddRequest 添加留言请求
     * @return 添加留言结果
     */
    @Override
    public Boolean addMessage(MessageAddRequest messageAddRequest) {
        // 添加留言频率限制
        ThrowUtils.throwIf(!rateLimiter.allowMessageAdd(messageAddRequest.getIp()),
                ErrorCode.OPERATION_ERROR, "添加留言频率过高，请稍后再试");
        Message message = new Message();
        message.setContent(messageAddRequest.getContent());
        message.setIp(messageAddRequest.getIp());
        boolean result =  this.save(message);
        if (result) {
            // 清除缓存
            redisTemplate.delete(MESSAGE_CACHE_KEY);
        }
        return result;
    }

    /**
     * 获取热门留言列表
     * @return 留言列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<MessageVO> getTop500() {
        // 查询留言频率限制
        ThrowUtils.throwIf(!rateLimiter.allowMessageQuery("system"),
                ErrorCode.OPERATION_ERROR, "获取热门留言频率过高，请稍后再试");
        // 从缓存中获取热门留言
        List<MessageVO> cachedMessages = (List<MessageVO>) redisTemplate.opsForValue().get(MESSAGE_CACHE_KEY);
        if (cachedMessages != null) {
            return cachedMessages;
        }
        // 若缓存未命中，从数据库中获取热门留言
        List<MessageVO> messages = this.baseMapper.getTop500();
        // 将热门留言放入缓存
        redisTemplate.opsForValue().set(MESSAGE_CACHE_KEY, messages, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return messages;
    }

    /**
     * 分页获取留言列表
     * @param messageQueryRequest 查询条件
     * @return 留言列表
     */
    @Override
    public Page<Message> getMessagePage(MessageQueryRequest messageQueryRequest) {
        // 分页查询留言频率限制
        ThrowUtils.throwIf(!rateLimiter.allowMessageQuery("system"),
                ErrorCode.OPERATION_ERROR, "获取留言列表频率过高，请稍后再试");
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        Page<Message> messagePage = new Page<>(current, size);
        QueryWrapper<Message> queryWrapper = this.getQueryWrapper(messageQueryRequest);
        return this.page(messagePage, queryWrapper);
    }

    /**
     * 获取查询条件
     * @param messageQueryRequest 查询留言请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<Message> getQueryWrapper(MessageQueryRequest messageQueryRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (messageQueryRequest == null) {
            return queryWrapper;
        }
        String ip = messageQueryRequest.getIp();
        String content = messageQueryRequest.getContent();
        String sortField = messageQueryRequest.getSortField();
        String sortOrder = messageQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(ip), "ip", ip);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 删除留言
     * @param id 留言id
     * @return 是否成功
     */
    @Override
    public boolean deleteMessage(long id) {
        Message message = this.getById(id);
        ThrowUtils.throwIf(message == null, ErrorCode.NOT_FOUND_ERROR, "留言不存在");
        boolean result = this.removeById(id);
        if (result) {
            // 清除缓存
            redisTemplate.delete(MESSAGE_CACHE_KEY);
        }
        return result;
    }
}
