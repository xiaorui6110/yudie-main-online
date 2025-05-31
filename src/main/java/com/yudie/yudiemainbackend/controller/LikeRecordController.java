package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.dto.like.LikeQueryRequest;
import com.yudie.yudiemainbackend.model.dto.like.LikeRequest;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.LikeRecordVO;
import com.yudie.yudiemainbackend.service.LikeRecordService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 点赞实现类接口
 * @author: xiaorui
 * @date: 2025-05-30 15:35
 **/
@Slf4j
@RestController
@RequestMapping("/like")
public class LikeRecordController {

    @Resource
    private UserService userService;

    @Resource
    private LikeRecordService likeRecordService;

    /**
     * 点赞
     * @param likeRequest 点赞请求
     * @param request 请求
     * @return 点赞结果
     */
    @PostMapping("/do")
    public BaseResponse<Boolean> doLike(@RequestBody LikeRequest likeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        try {
            likeRecordService.doLike(likeRequest, loginUser.getId());
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("Error in doLike controller: ", e);
            return ResultUtils.success(false);
        }
    }

    /**
     * 获取点赞状态
     * @param targetType 类型
     * @param targetId ID
     * @param request 请求
     * @return 点赞状态
     */
    @GetMapping("/status/{targetType}/{targetId}")
    public BaseResponse<Boolean> getLikeStatus(
            @PathVariable("targetType") Integer targetType,
            @PathVariable("targetId") Long targetId,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        boolean isLiked = likeRecordService.isContentLiked(targetId, targetType, loginUser.getId());
        return ResultUtils.success(isLiked);
    }

    /**
     * 获取未读点赞记录
     * @param request 请求
     * @return 未读点赞记录
     */
    @GetMapping("/unread")
    public BaseResponse<List<LikeRecordVO>> getUnreadLikes(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        List<LikeRecordVO> unreadLikes = likeRecordService.getAndClearUnreadLikes(loginUser.getId());
        return ResultUtils.success(unreadLikes);
    }

    /**
     * 获取点赞历史
     * @param likeQueryRequest 点赞查询请求
     * @param request 请求
     * @return 点赞历史
     */
    @PostMapping("/history")
    public BaseResponse<Page<LikeRecordVO>> getLikeHistory(@RequestBody LikeQueryRequest likeQueryRequest,
                                                           HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long size = likeQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<LikeRecordVO> likeHistory = likeRecordService.getUserLikeHistory(likeQueryRequest, loginUser.getId());
        return ResultUtils.success(likeHistory);
    }

    /**
     * 获取我的点赞历史
     * @param likeQueryRequest 点赞查询请求
     * @param request 请求
     * @return 我的点赞历史
     */
    @PostMapping("/my/history")
    public BaseResponse<Page<LikeRecordVO>> getMyLikeHistory(@RequestBody LikeQueryRequest likeQueryRequest,
                                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long size = likeQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<LikeRecordVO> likeHistory = likeRecordService.getMyLikeHistory(likeQueryRequest, loginUser.getId());
        return ResultUtils.success(likeHistory);
    }

    /**
     * 获取未读点赞数
     * @param request 请求
     * @return 未读点赞数
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadLikesCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return ResultUtils.success(likeRecordService.getUnreadLikesCount(loginUser.getId()));
    }

}
