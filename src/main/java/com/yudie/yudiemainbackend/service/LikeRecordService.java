package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.dto.like.LikeQueryRequest;
import com.yudie.yudiemainbackend.model.dto.like.LikeRequest;
import com.yudie.yudiemainbackend.model.entity.LikeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.vo.LikeRecordVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
* @author xiaorui
* @description 针对表【like_record(点赞记录表)】的数据库操作Service
* @createDate 2025-05-30 14:27:05
*/
public interface LikeRecordService extends IService<LikeRecord> {

    /**
     * 通用点赞/取消点赞
     * @param likeRequest 点赞请求
     * @param userId 用户ID
     * @return 点赞结果
     */
    CompletableFuture<Boolean> doLike(LikeRequest likeRequest, Long userId);

    /**
     * 获取并清除用户未读的点赞消息
     * @param userId 用户ID
     * @return 点赞消息列表
     */
    List<LikeRecordVO> getAndClearUnreadLikes(Long userId);

    /**
     * 获取用户的点赞历史（分页）
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 点赞历史列表
     */
    Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, Long userId);

    /**
     * 检查内容是否已被用户点赞
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param userId 用户ID
     * @return 是否已被点赞
     */
    boolean isContentLiked(Long targetId, Integer targetType, Long userId);

    /**
     * 获取用户未读点赞数
     * @param userId 用户ID
     * @return 未读点赞数
     */
    long getUnreadLikesCount(Long userId);

    /**
     * 清除用户所有未读点赞状态
     * @param userId 用户ID
     */
    void clearAllUnreadLikes(Long userId);

    /**
     * 获取用户自己的点赞历史（分页）
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 点赞历史列表
     */
    Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, Long userId);

}
