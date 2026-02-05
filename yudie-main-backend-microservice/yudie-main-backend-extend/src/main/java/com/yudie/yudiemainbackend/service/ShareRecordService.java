package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.dto.share.ShareQueryRequest;
import com.yudie.yudiemainbackend.model.dto.share.ShareRequest;
import com.yudie.yudiemainbackend.model.entity.ShareRecord;
import com.yudie.yudiemainbackend.model.vo.ShareRecordVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
* @author xiaorui
* @description 针对表【share_record(分享记录表)】的数据库操作Service
* @createDate 2025-05-30 14:28:41
*/
public interface ShareRecordService extends IService<ShareRecord> {

    /**
     * 分享
     * @param shareRequest 分享请求
     * @param userId 用户ID
     * @return 分享结果
     */
    CompletableFuture<Boolean> doShare(ShareRequest shareRequest, Long userId);

    /**
     * 获取并清除用户未读的分享消息
     * @param userId 用户ID
     * @return 分享记录
     */
    List<ShareRecordVO> getAndClearUnreadShares(Long userId);

    /**
     * 获取用户分享历史（分页）
     * @param shareQueryRequest 查询分享请求
     * @param userId 用户ID
     * @return 分享记录
     */
    Page<ShareRecordVO> getUserShareHistory(ShareQueryRequest shareQueryRequest, Long userId);

    /**
     * 判断内容是否被分享过
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param userId 用户ID
     * @return 是否被分享过
     */
    boolean isContentShared(Long targetId, Integer targetType, Long userId);

    /**
     * 获取用户未读分享数量
     * @param userId 用户ID
     * @return 未读分享数量
     */
    long getUnreadSharesCount(Long userId);

    /**
     * 清除用户所有未读分享状态
     * @param userId 用户ID
     */
    void clearAllUnreadShares(Long userId);

    /**
     * 获取用户自己的分享历史（分页）
     * @param shareQueryRequest 查询分享请求
     * @param userId 用户ID
     * @return 分享记录
     */
    Page<ShareRecordVO> getMyShareHistory(ShareQueryRequest shareQueryRequest, Long userId);

}
