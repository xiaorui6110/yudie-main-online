package com.yudie.yudiemainbackend.controller;

import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.innerservice.extend.InnerLikeRecordService;
import com.yudie.yudiemainbackend.innerservice.extend.InnerShareRecordService;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserService;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.MessageCenterVO;
import com.yudie.yudiemainbackend.service.CommentsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @description: 消息中心实现类接口
 * @author: xiaorui
 * @date: 2025-05-31 10:50
 **/
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageCenterController {

    @Resource
    private InnerUserService userService;

    @Resource
    private CommentsService commentsService;

    @Resource
    private InnerLikeRecordService likeRecordService;

    @Resource
    private InnerShareRecordService shareRecordService;

    /**
     * 获取消息中心未读数据
     * @param request 请求
     */
    @GetMapping("/unread/count")
    public BaseResponse<MessageCenterVO> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        MessageCenterVO messageCenterVO = new MessageCenterVO();
        long unreadComments = commentsService.getUnreadCommentsCount(loginUser.getId());
        long unreadLikes = likeRecordService.getUnreadLikesCount(loginUser.getId());
        long unreadShares = shareRecordService.getUnreadSharesCount(loginUser.getId());
        messageCenterVO.setUnreadComments(unreadComments);
        messageCenterVO.setUnreadLikes(unreadLikes);
        messageCenterVO.setUnreadShares(unreadShares);
        messageCenterVO.setTotalUnread(unreadComments + unreadLikes + unreadShares);
        return ResultUtils.success(messageCenterVO);
    }

    /**
     * 将所有消息标记为已读
     * @param request 请求
     */
    @PostMapping("/read/all")
    public BaseResponse<Boolean> markAllAsRead(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        try {
            commentsService.clearAllUnreadComments(loginUser.getId());
            likeRecordService.clearAllUnreadLikes(loginUser.getId());
            shareRecordService.clearAllUnreadShares(loginUser.getId());
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("Error in markAllAsRead: ", e);
            return ResultUtils.success(false);
        }
    }

}
