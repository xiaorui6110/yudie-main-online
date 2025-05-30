package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.mapper.PictureMapper;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsAddRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsDeleteRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsLikeRequest;
import com.yudie.yudiemainbackend.model.dto.comments.CommentsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Comments;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.CommentUserVO;
import com.yudie.yudiemainbackend.model.vo.CommentsVO;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private PictureMapper pictureMapper;

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
        // 判断是否登录
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 查询评论信息
        Comments comments = this.getById(commentsDeleteRequest.getCommentId());
        if (comments == null || comments.getIsDelete()== 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        // 判断是否是本人（只能删除自己的评论）
        if (!loginUser.getId().equals(comments.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        // 先计算要删除的评论及其子评论的总数
        int deletedCommentCount = countCommentsRecursively(comments.getCommentId());

        // 删除评论及其子评论
        boolean success = deleteCommentsRecursively(comments.getCommentId());
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除评论失败");
        }

        // 更新 MySQL 中的评论数
        switch (comments.getTargetType()) {
            // 图片
            case 1:
                pictureService.update()
                        .setSql("commentCount = commentCount - " + deletedCommentCount)
                        .eq("id", comments.getTargetId())
                        .ge("commentCount", deletedCommentCount)
                        .update();
                // TODO 更新 ES 中图片的评论数 updateEsPictureCommentCount
                //updateEsPictureCommentCount(comments.getTargetId(), -deletedCommentCount);
                break;
            // 帖子
            case 2:
                postService.update()
                        .setSql("commentCount = commentCount - " + deletedCommentCount)
                        .eq("id", comments.getTargetId())
                        .ge("commentCount", deletedCommentCount)
                        .update();
                // TODO 更新 ES 中帖子的评论数 updateEsPostCommentCount
                //updateEsPostCommentCount(comments.getTargetId(), -deletedCommentCount);
                break;
            default:
                log.error("Unsupported target type: {}", comments.getTargetType());
        }

        return true;
    }

    /**
     * 递归计算评论及其子评论的数量
     * @param commentId 评论ID
     * @return 评论及其子评论的总数
     */
    private int countCommentsRecursively(Long commentId) {
        // 获取所有未删除的子评论
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentCommentId", commentId)
                .eq("isDelete", 0);
        List<Comments> childComments = this.list(queryWrapper);
        // 计算子评论总数
        int count = 1;
        if (!childComments.isEmpty()) {
            for (Comments childComment : childComments) {
                count += countCommentsRecursively(childComment.getCommentId());
            }
        }
        return count;
    }

    /**
     * 递归删除评论及其子评论
     * @param commentId 评论ID
     * @return 删除结果
     */
    private boolean deleteCommentsRecursively(Long commentId) {
        // 获取所有未删除的子评论
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentCommentId", commentId)
                .eq("isDelete", 0);
        List<Comments> childComments = this.list(queryWrapper);
        // 递归删除子评论
        for (Comments childComment : childComments) {
            if (!deleteCommentsRecursively(childComment.getCommentId())) {
                return false;
            }
        }
        // 删除当前评论
        return this.update(new UpdateWrapper<Comments>()
                .eq("commentId", commentId)
                .eq("isDelete", 0)
                .set("isDelete", 1));
    }

    /**
     * 查询评论
     * @param commentsQueryRequest 查询评论请求
     * @param request 请求
     * @return 评论列表
     */
    @Override
    public Page<CommentsVO> queryComment(CommentsQueryRequest commentsQueryRequest, HttpServletRequest request) {
        // 判断是否登录
        User user = userService.getLoginUser(request);
        if (user == null) {
            return null;
        }
        long current = commentsQueryRequest.getCurrent();
        long size = commentsQueryRequest.getPageSize();
        Page<Comments> page = new Page<>(current, size);
        // 判断是否传递图片id
        ThrowUtils.throwIf(commentsQueryRequest.getTargetId() == null, ErrorCode.PARAMS_ERROR,
                "目标id不能为空");
        // 查询顶级评论
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetId", commentsQueryRequest.getTargetId())
                // 默认查询图片评论
                .eq("targetType", commentsQueryRequest.getTargetType() != null ?
                        commentsQueryRequest.getTargetType() : 1)
                .eq("parentCommentId", 0)
                .orderByDesc("createTime");
        // 查询评论是否存在，不存在返回空
        if (count(queryWrapper) == 0) {
            return null;
        }
        // 得到顶级评论列表
        Page<Comments> commentsPage = page(page, queryWrapper);
        // 获取评论用户的 ID 列表
        List<Long> userIds = commentsPage.getRecords().stream()
                .map(Comments::getUserId)
                .collect(Collectors.toList());
        // 批量查询评论用户信息，先检查 userIds 不为空
        if (userIds.isEmpty()) {
            return new PageDTO<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        }
        // 批量查询评论用户信息
        List<User> users = userService.listByIds(userIds);
        // 将 User 列表转换为 commentUserVO 列表
        List<CommentUserVO> commentUserVOs = users.stream().map(user1 -> {
            CommentUserVO commentUserVO = new CommentUserVO();
            BeanUtils.copyProperties(user1, commentUserVO);
            return commentUserVO;
        }).collect(Collectors.toList());
        // 将 Comments 列表转换为 CommentsVO 列表
        Map<Long, CommentUserVO> userMap = commentUserVOs.stream()
                .collect(Collectors.toMap(CommentUserVO::getId, CommentUserVO -> CommentUserVO));
        List<CommentsVO> commentsVOList = commentsPage.getRecords().stream().map(comments -> {
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comments, commentsVO);
            // 查找对应的评论用户信息
            CommentUserVO commentUserVO = userMap.get(comments.getUserId());
            if (commentUserVO!= null) {
                commentsVO.setCommentUser(commentUserVO);
            }
            // 递归查询子评论
            commentsVO.setChildren(getChildrenComments(comments.getCommentId()));
            return commentsVO;
        }).collect(Collectors.toList());
        Page<CommentsVO> resultPage = new PageDTO<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        resultPage.setRecords(commentsVOList);
        return resultPage;
    }

    /**
     * 递归查询子评论
     * @param parentCommentId 顶级评论ID
     * @return 子评论列表
     */
    private List<CommentsVO> getChildrenComments(Long parentCommentId) {
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentCommentId", parentCommentId);
        // 按照创建时间倒序排列
        queryWrapper.orderByDesc("createTime");
        // 使用 CommentsService 的 list 方法查询子评论
        List<Comments> childrenComments = this.list(queryWrapper);
        if (childrenComments == null || childrenComments.isEmpty()) {
            return Collections.emptyList();
        }
        // 获取子评论的用户 ID 列表
        List<Long> childUserIds = childrenComments.stream()
                .map(Comments::getUserId)
                .collect(Collectors.toList());
        // 批量查询子评论的用户信息
        List<User> childUsers = userService.listByIds(childUserIds);
        List<CommentUserVO> childCommentUserVOs = childUsers.stream().map(user -> {
            CommentUserVO commentUserVO = new CommentUserVO();
            BeanUtils.copyProperties(user, commentUserVO);
            return commentUserVO;
        }).collect(Collectors.toList());
        Map<Long, CommentUserVO> childUserMap = childCommentUserVOs.stream()
                .collect(Collectors.toMap(CommentUserVO::getId, CommentUserVO -> CommentUserVO));
        return childrenComments.stream().map(comments -> {
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comments, commentsVO);
            // 查找对应的子评论用户信息
            CommentUserVO commentUserVO = childUserMap.get(comments.getUserId());
            if (commentUserVO!= null) {
                commentsVO.setCommentUser(commentUserVO);
            }
            // 递归调用，查询子评论的子评论
            commentsVO.setChildren(getChildrenComments(comments.getCommentId()));
            return commentsVO;
        }).collect(Collectors.toList());
    }

    /**
     * 点赞评论
     * @param commentslikeRequest 点赞评论请求
     * @param request 请求
     * @return 点赞结果
     */
    @Override
    public Boolean likeComment(CommentsLikeRequest commentslikeRequest, HttpServletRequest request) {
        // 检查评论 ID 是否为空
        ThrowUtils.throwIf(commentslikeRequest.getCommentId() == null, ErrorCode.PARAMS_ERROR, "评论 id 不能为空");
        // 获取用户信息
        User user = (User) request.getSession().getAttribute("user");
        // 创建更新包装器
        UpdateWrapper<Comments> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("commentId", commentslikeRequest.getCommentId());
        // 获取评论信息
        Comments comments = getOne(updateWrapper);
        if (comments == null) {
            return false;
        }
        if (commentslikeRequest.getLikeCount() != null && commentslikeRequest.getLikeCount() != 0) {
            // 处理点赞操作
            updateWrapper.setSql("likeCount = likeCount + " + commentslikeRequest.getLikeCount());
        }
        if(commentslikeRequest.getDislikeCount() != null && commentslikeRequest.getDislikeCount() != 0) {
            // 处理点踩操作
            updateWrapper.setSql("dislikeCount = dislikeCount + " + commentslikeRequest.getDislikeCount());
        }
        // 执行更新操作
        return update(updateWrapper);
    }

    /**
     * 获取未读评论
     * @param userId 用户ID
     * @return 未读评论列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CommentsVO> getAndClearUnreadComments(Long userId) {
        // 1. 获取未读评论记录
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetUserId", userId)
                .eq("isRead", 0)
                // 添加这一行，排除自己评论自己的记录
                .ne("userId", userId)
                .orderByDesc("createTime");
        List<Comments> unreadComments = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }
        // 2. 批量更新为已读
        List<Long> commentIds = unreadComments.stream()
                .map(Comments::getCommentId)
                .collect(Collectors.toList());
        this.update(new UpdateWrapper<Comments>()
                .set("isRead", 1)
                .in("commentId", commentIds));
        // 3. 构建返回数据
        return unreadComments.stream().map(comment -> {
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comment, commentsVO);
            // 获取评论用户信息
            User commentUser = userService.getById(comment.getUserId());
            if (commentUser != null) {
                CommentUserVO commentUserVO = new CommentUserVO();
                BeanUtils.copyProperties(commentUser, commentUserVO);
                commentsVO.setCommentUser(commentUserVO);
            }
            // 根据目标类型获取不同内容
            switch (comment.getTargetType()) {
                // 图片
                case 1:
                    Picture picture = pictureMapper.selectById(comment.getTargetId());
                    if (picture != null) {
                        commentsVO.setPicture(PictureVO.objToVo(picture));
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            commentsVO.getPicture().setUser(userService.getUserVO(pictureUser));
                        }
                    }
                    break;
                // 帖子
                case 2:
                    Post post = postService.getById(comment.getTargetId());
                    if (post != null) {
                        // 设置帖子作者信息
                        User postUser = userService.getById(post.getUserId());
                        if (postUser != null) {
                            post.setUser(userService.getUserVO(postUser));
                        }
                        commentsVO.setPost(post);
                    }
                    break;
                default:
                    log.error("Unsupported target type: {}", comment.getTargetType());
                    break;
            }
            // 递归获取子评论
            commentsVO.setChildren(getChildrenComments(comment.getCommentId()));
            return commentsVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取未读评论数量
     * @param userId 用户ID
     * @return 未读评论数量
     */
    @Override
    public long getUnreadCommentsCount(Long userId) {
        return this.count(new QueryWrapper<Comments>()
                .eq("targetUserId", userId)
                .eq("isRead", 0)
                // 添加这一行，排除自己评论自己的记录
                .ne("userId", userId));
    }

    /**
     * 清空所有未读评论
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadComments(Long userId) {
        this.update(new UpdateWrapper<Comments>()
                .set("isRead", 1)
                .eq("targetUserId", userId)
                .eq("isRead", 0));
    }

    /**
     * 获取用户评论历史
     * @param commentsQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 评论列表
     */
    @Override
    public Page<CommentsVO> getCommentedHistory(CommentsQueryRequest commentsQueryRequest, Long userId) {
        ThrowUtils.throwIf(commentsQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = commentsQueryRequest.getCurrent();
        long size = commentsQueryRequest.getPageSize();
        // 创建分页对象
        Page<Comments> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        // 查询用户发出的评论
        queryWrapper.eq("userId", userId)
                // 只查询未删除的评论
                .eq("isDelete", 0);
        // 处理目标类型查询
        Integer targetType = commentsQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }
        queryWrapper.orderByDesc("createTime");
        // 执行分页查询
        Page<Comments> commentsPage = this.page(page, queryWrapper);
        // 转换结果
        List<CommentsVO> records = commentsPage.getRecords().stream().map(comment -> {
            CommentsVO vo = new CommentsVO();
            BeanUtils.copyProperties(comment, vo);
            // 设置评论用户信息
            User commentUser = userService.getById(comment.getUserId());
            if (commentUser != null) {
                CommentUserVO commentUserVO = new CommentUserVO();
                BeanUtils.copyProperties(commentUser, commentUserVO);
                vo.setCommentUser(commentUserVO);
            }
            // 根据目标类型获取不同内容
            switch (comment.getTargetType()) {
                // 图片
                case 1:
                    Picture picture = pictureService.getById(comment.getTargetId());
                    if (picture != null) {
                        vo.setPicture(PictureVO.objToVo(picture));
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            vo.getPicture().setUser(userService.getUserVO(pictureUser));
                        }
                    }
                    break;
                // 帖子
                case 2:
                    Post post = postService.getById(comment.getTargetId());
                    if (post != null) {
                        // 设置帖子作者信息
                        User postUser = userService.getById(post.getUserId());
                        if (postUser != null) {
                            post.setUser(userService.getUserVO(postUser));
                        }
                        vo.setPost(post);
                    }
                    break;
                default:
                    log.error("Unsupported target type: {}", comment.getTargetType());
                    break;
            }
            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Page<CommentsVO> voPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    /**
     * 获取我的评论历史
     * @param commentsQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 评论列表
     */
    @Override
    public Page<CommentsVO> getMyCommentHistory(CommentsQueryRequest commentsQueryRequest, Long userId) {
        ThrowUtils.throwIf(commentsQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = commentsQueryRequest.getCurrent();
        long size = commentsQueryRequest.getPageSize();
        // 创建分页对象
        Page<Comments> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        // 查询用户收到的评论
        queryWrapper.eq("targetUserId", userId)
                // 只查询未删除的评论
                .eq("isDelete", 0)
                // 排除自己评论自己的记录
                .ne("userId", userId);
        // 处理目标类型查询
        Integer targetType = commentsQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }
        queryWrapper.orderByDesc("createTime");
        // 执行分页查询
        Page<Comments> commentsPage = this.page(page, queryWrapper);
        // 转换结果
        List<CommentsVO> records = commentsPage.getRecords().stream().map(comment -> {
            CommentsVO vo = new CommentsVO();
            BeanUtils.copyProperties(comment, vo);
            // 设置评论用户信息
            User commentUser = userService.getById(comment.getUserId());
            if (commentUser != null) {
                CommentUserVO commentUserVO = new CommentUserVO();
                BeanUtils.copyProperties(commentUser, commentUserVO);
                vo.setCommentUser(commentUserVO);
            }
            // 根据目标类型获取不同内容
            switch (comment.getTargetType()) {
                // 图片
                case 1:
                    Picture picture = pictureService.getById(comment.getTargetId());
                    if (picture != null) {
                        vo.setPicture(PictureVO.objToVo(picture));
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            vo.getPicture().setUser(userService.getUserVO(pictureUser));
                        }
                    }
                    break;
                // 帖子
                case 2:
                    Post post = postService.getById(comment.getTargetId());
                    if (post != null) {
                        // 设置帖子作者信息
                        User postUser = userService.getById(post.getUserId());
                        if (postUser != null) {
                            post.setUser(userService.getUserVO(postUser));
                        }
                        vo.setPost(post);
                    }
                    break;
                default:
                    log.error("Unsupported target type: {}", comment.getTargetType());
                    break;
            }
            return vo;
        }).collect(Collectors.toList());
        // 构建返回结果
        Page<CommentsVO> voPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

}
