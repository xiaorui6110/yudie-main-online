package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.dto.share.ShareQueryRequest;
import com.yudie.yudiemainbackend.model.dto.share.ShareRequest;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.ShareRecordVO;
import com.yudie.yudiemainbackend.service.ShareRecordService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @description:
 * @author: siri
 * @date: 2025-05-30 16:02
 **/
@Slf4j
@RestController
@RequestMapping("/share")
public class ShareRecordController {

    @Resource
    private UserService userService;

    @Resource
    private ShareRecordService shareRecordService;

    /**
     * 分享
     * @param shareRequest 分享请求
     * @param request 请求
     * @return 分享结果
     */
    @PostMapping("/do")
    public BaseResponse<Boolean> doShare(@RequestBody ShareRequest shareRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        try {
            CompletableFuture<Boolean> future = shareRecordService.doShare(shareRequest, loginUser.getId());
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("Error in doShare controller: ", e);
            return ResultUtils.success(false);
        }
    }

    /**
     * 获取分享状态
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param request 请求
     * @return 分享状态
     */
    @GetMapping("/status/{targetType}/{targetId}")
    public BaseResponse<Boolean> getShareStatus(
            @PathVariable("targetType") Integer targetType,
            @PathVariable("targetId") Long targetId,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        boolean isShared = shareRecordService.isContentShared(targetId, targetType, loginUser.getId());
        return ResultUtils.success(isShared);
    }

    /**
     * 获取未读分享
     * @param request 请求
     * @return 未读分享
     */
    @GetMapping("/unread")
    public BaseResponse<List<ShareRecordVO>> getUnreadShares(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        List<ShareRecordVO> unreadShares = shareRecordService.getAndClearUnreadShares(loginUser.getId());
        return ResultUtils.success(unreadShares);
    }

    /**
     * 获取用户被分享历史
     * @param shareQueryRequest 查询分享请求
     * @param request 请求
     * @return 分享历史列表
     */
    @PostMapping("/history")
    public BaseResponse<Page<ShareRecordVO>> getShareHistory(@RequestBody ShareQueryRequest shareQueryRequest,
                                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long size = shareQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ShareRecordVO> shareHistory = shareRecordService.getUserShareHistory(shareQueryRequest, loginUser.getId());
        return ResultUtils.success(shareHistory);
    }

    /**
     * 获取我的分享历史
     * @param shareQueryRequest 查询分享请求
     * @param request 请求
     * @return 我的分享历史
     */
    @PostMapping("/my/history")
    public BaseResponse<Page<ShareRecordVO>> getMyShareHistory(@RequestBody ShareQueryRequest shareQueryRequest,
                                                               HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long size = shareQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ShareRecordVO> shareHistory = shareRecordService.getMyShareHistory(shareQueryRequest, loginUser.getId());
        return ResultUtils.success(shareHistory);
    }

    /**
     * 获取未读分享数
     * @param request 请求
     * @return 未读分享数
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadSharesCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return ResultUtils.success(shareRecordService.getUnreadSharesCount(loginUser.getId()));
    }

}
