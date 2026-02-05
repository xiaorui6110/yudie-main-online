package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.constant.CrawlerConstant;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsAddRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsDeleteRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsLikeRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.CommentsVO;
import com.yudie.yudiemainbackend.service.CommentsService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 评论实现类接口
 * @author: xiaorui
 * @date: 2025-05-30 09:46
 **/
@Slf4j
@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Resource
    private UserService userService;

    @Resource
    private CommentsService commentsService;

    @Resource
    private CrawlerManager crawlerManager;

    /**
     * 添加评论
     * @param commentsAddRequest 评论内容请求
     * @param request HTTP请求
     * @return 添加结果
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addComment(@RequestBody CommentsAddRequest commentsAddRequest, HttpServletRequest request) {
        crawlerManager.detectFrequentRequest(request);
        return ResultUtils.success(commentsService.addComments(commentsAddRequest, request));
    }

    /**
     * 删除评论
     * @param commentsDeleteRequest 删除评论请求
     * @param request HTTP请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody CommentsDeleteRequest commentsDeleteRequest, HttpServletRequest request) {
        crawlerManager.detectFrequentRequest(request);
        return ResultUtils.success(commentsService.deleteComment(commentsDeleteRequest, request));
    }

    /**
     * 查询指定图片的评论列表
     * @param commentsQueryRequest 评论查询参数
     * @param request HTTP请求
     * @return 评论列表（分页）
     */
    @PostMapping("/query")
    public BaseResponse<Page<CommentsVO>> queryComment(@RequestBody CommentsQueryRequest commentsQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser != null) {
            String userRole = loginUser.getUserRole();
            ThrowUtils.throwIf(userRole.equals(CrawlerConstant.BAN_ROLE),
                    ErrorCode.NOT_AUTH_ERROR, "封禁用户禁止获取数据,请联系管理员");
        }
        long size = commentsQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        crawlerManager.detectNormalRequest(request);
        return ResultUtils.success(commentsService.queryComment(commentsQueryRequest, request));
    }

    /**
     * 点赞评论
     * @param commentslikeRequest 评论点赞请求
     * @param request HTTP请求
     * @return 点赞结果
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> likeComment(@RequestBody CommentsLikeRequest commentslikeRequest, HttpServletRequest request) {
        crawlerManager.detectFrequentRequest(request);
        return ResultUtils.success(commentsService.likeComment(commentslikeRequest, request));
    }

    /**
     * 获取未读评论列表
     * @param request HTTP请求
     * @return 未读评论列表
     */
    @GetMapping("/unread")
    public BaseResponse<List<CommentsVO>> getUnreadComments(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        crawlerManager.detectNormalRequest(request);
        List<CommentsVO> unreadComments = commentsService.getAndClearUnreadComments(loginUser.getId());
        return ResultUtils.success(unreadComments);
    }

    /**
     * 获取未读评论数量
     * @param request HTTP请求
     * @return 未读评论数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadCommentsCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        crawlerManager.detectNormalRequest(request);
        return ResultUtils.success(commentsService.getUnreadCommentsCount(loginUser.getId()));
    }

    /**
     * 获取我的评论历史
     * @param commentsQueryRequest 评论查询参数
     * @param request HTTP请求
     * @return 我的评论历史（分页）
     */
    @PostMapping("/my/history")
    public BaseResponse<Page<CommentsVO>> getMyCommentHistory(@RequestBody CommentsQueryRequest commentsQueryRequest,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long size = commentsQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        crawlerManager.detectNormalRequest(request);
        Page<CommentsVO> commentHistory = commentsService.getMyCommentHistory(commentsQueryRequest, loginUser.getId());
        return ResultUtils.success(commentHistory);
    }

    /**
     * 获取评论我的历史
     * @param commentsQueryRequest 评论查询参数
     * @param request HTTP请求
     * @return 评论我的历史（分页）
     */
    @PostMapping("/commented/history")
    public BaseResponse<Page<CommentsVO>> getCommentedHistory(@RequestBody CommentsQueryRequest commentsQueryRequest,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long size = commentsQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        crawlerManager.detectNormalRequest(request);
        Page<CommentsVO> commentHistory = commentsService.getCommentedHistory(commentsQueryRequest, loginUser.getId());
        return ResultUtils.success(commentHistory);
    }

}
