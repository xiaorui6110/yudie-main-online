package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsAddRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsDeleteRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsLikeRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Comments;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.CommentsVO;
import com.yudie.yudiemainbackend.service.CommentsService;
import com.yudie.yudiemainbackend.mapper.CommentsMapper;
import com.yudie.yudiemainbackend.service.PictureService;
import com.yudie.yudiemainbackend.service.PostService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lenovo
* @description 针对表【comments(评论表)】的数据库操作Service实现
* @createDate 2025-05-29 22:11:04
*/
@Slf4j
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
    implements CommentsService{

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private PostService postService;

    /**
     * 添加评论
     * @param commentsAddRequest 添加评论请求
     * @param request 请求
     * @return 添加评论结果
     */
    @Override
    public Boolean addComments(CommentsAddRequest commentsAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 获取目标评论内容所属的用户ID
        Long targetUserId;
        switch (commentsAddRequest.getTargetType()) {
            // 图片
            case 1:
                Picture picture = pictureService.getById(commentsAddRequest.getTargetId());
                if (picture == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
                }
                targetUserId = picture.getUserId();
                break;
            // 帖子
            case 2:
                Post post = postService.getById(commentsAddRequest.getTargetId());
                if (post == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
                }
                targetUserId = post.getUserId();
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的评论类型");
        }
        Comments comments = new Comments();
        BeanUtils.copyProperties(commentsAddRequest, comments);
        // 设置评论用户ID
        comments.setUserId(loginUser.getId());
        comments.setTargetUserId(targetUserId);
        comments.setIsRead(0);
        // 设置初始点赞数
        comments.setLikeCount(0L);
        // 设置初始点踩数
        comments.setDislikeCount(0L);
        comments.setIsDelete(0);
        boolean result = save(comments);
        if (!result) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "评论保存失败");
        }
        // 更新评论数
        updateCommentCount(commentsAddRequest.getTargetId(), commentsAddRequest.getTargetType(), 1);
        return true;
    }

    /**
     * 更新评论数
     * @param targetId 目标评论ID
     * @param targetType 目标评论类型
     * @param delta 评论数变化量
     */
    private void updateCommentCount(Long targetId, Integer targetType, int delta) {
        switch (targetType) {
            // 图片
            case 1:
                pictureService.update()
                        .setSql("commentCount = commentCount + " + delta)
                        .eq("id", targetId)
                        .ge("commentCount", -delta)
                        .update();
                // TODO 更新 ES 中图片的评论数 updateEsPictureCommentCount
                //updateEsPictureCommentCount(targetId, delta);
                break;
            // 帖子
            case 2:
                postService.update()
                        .setSql("commentCount = commentCount + " + delta)
                        .eq("id", targetId)
                        .ge("commentCount", -delta)
                        .update();
                // TODO 更新 ES 中帖子的评论数 updateEsPostCommentCount
                //updateEsPostCommentCount(targetId, delta);
                break;
            default:
                log.error("Unsupported target type: {}", targetType);
        }
    }

    //TODO 更新 ES 中图片的评论数 updateEsPictureCommentCount

    //TODO 更新 ES 中帖子的评论数 updateEsPostCommentCount


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(CommentsDeleteRequest commentsDeleteRequest, HttpServletRequest request) {


        return null;
    }

    @Override
    public Page<CommentsVO> queryComment(CommentsQueryRequest commentsQueryRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean likeComment(CommentsLikeRequest commentslikeRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public List<CommentsVO> getAndClearUnreadComments(Long userId) {
        return null;
    }

    @Override
    public long getUnreadCommentsCount(Long userId) {
        return 0;
    }

    @Override
    public void clearAllUnreadComments(Long userId) {

    }

    @Override
    public Page<CommentsVO> getCommentedHistory(CommentsQueryRequest commentsQueryRequest, Long id) {
        return null;
    }

    @Override
    public Page<CommentsVO> getMyCommentHistory(CommentsQueryRequest commentsQueryRequest, Long id) {
        return null;
    }
}




