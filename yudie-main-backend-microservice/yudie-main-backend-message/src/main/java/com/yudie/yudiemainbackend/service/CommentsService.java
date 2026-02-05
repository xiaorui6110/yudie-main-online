package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsAddRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsDeleteRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsLikeRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Comments;
import com.yudie.yudiemainbackend.model.vo.CommentsVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【comments(评论表)】的数据库操作Service
* @createDate 2025-05-29 22:11:04
*/
public interface CommentsService extends IService<Comments> {

    /**
     * 添加评论
     * @param commentsAddRequest 添加评论请求
     * @param request 请求
     * @return 添加评论结果
     */
    Boolean addComments(CommentsAddRequest commentsAddRequest, HttpServletRequest request);

    /**
     * 删除评论
     * @param commentsDeleteRequest 删除评论请求
     * @param request 请求
     * @return 删除评论结果
     */
    Boolean deleteComment(CommentsDeleteRequest commentsDeleteRequest, HttpServletRequest request);

    /**
     * 查询评论
     * @param commentsQueryRequest 查询评论请求
     * @param request 请求
     * @return 查询评论结果
     */
    Page<CommentsVO> queryComment(CommentsQueryRequest commentsQueryRequest, HttpServletRequest request);

    /**
     * 点赞评论
     * @param commentslikeRequest 点赞评论请求
     * @param request 请求
     * @return 点赞评论结果
     */
    Boolean likeComment(CommentsLikeRequest commentslikeRequest, HttpServletRequest request);

    /**
     * 获取并清除用户未读的评论消息
     * @param userId 用户ID
     * @return 未读的评论消息列表
     */
    List<CommentsVO> getAndClearUnreadComments(Long userId);

    /**
     * 获取用户未读评论数量
     * @param userId 用户ID
     * @return 未读评论数量
     */
    long getUnreadCommentsCount(Long userId);

    /**
     * 清空用户未读评论
     * @param userId 用户ID
     */
    void clearAllUnreadComments(Long userId);

    /**
     * 获取评论历史
     * @param commentsQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 评论历史
     */
    Page<CommentsVO> getCommentedHistory(CommentsQueryRequest commentsQueryRequest, Long userId);

    /**
     * 获取我的评论历史
     * @param commentsQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 我的评论历史
     */
    Page<CommentsVO> getMyCommentHistory(CommentsQueryRequest commentsQueryRequest, Long userId);

}
