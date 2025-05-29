package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.dto.post.PostAddRequest;
import com.yudie.yudiemainbackend.model.dto.post.PostQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lenovo
* @description 针对表【post(论坛帖子表)】的数据库操作Service
* @createDate 2025-05-29 15:36:22
*/
public interface PostService extends IService<Post> {

    /**
     * 发布帖子
     * @param postAddRequest 发布帖子请求
     * @param loginUser 用户
     * @return 帖子 id
     */
    Long addPost(PostAddRequest postAddRequest, User loginUser);

    /**
     * 分页获取帖子列表（直接使用 Post 是因为确实没有什么需要封装的信息，都可以直接展示给用户）
     * @param postQueryRequest 获取帖子列表请求
     * @param loginUser 用户
     * @return 帖子列表
     */
    Page<Post> getPostList(PostQueryRequest postQueryRequest, User loginUser);

    /**
     * 审核帖子
     * @param postId 帖子 id
     * @param status 帖子状态
     * @param message 审核信息
     * @param loginUser 用户
     */
    void reviewPost(Long postId, Integer status, String message, User loginUser);

    /**
     * 获取我的帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @return 我的帖子列表
     */
    Page<Post> getMyPostList(PostQueryRequest postQueryRequest);

    /**
     * 更新帖子
     * @param post 帖子
     * @return 更新结果
     */
    boolean updatePost(Post post);

    /**
     * 获取关注用户的帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @param request 请求
     * @return 关注用户的帖子列表
     */
    Page<Post> getFollowPosts(PostQueryRequest postQueryRequest, HttpServletRequest request);

    /**
     * 获取帖子排行榜
     * @param type 榜单类型：1-日榜 2-周榜 3-月榜 4-总榜
     * @return 帖子排行榜
     */
    List<Post> getTop100Post(Long type);

    /**
     * 填充帖子信息
     * @param post 帖子
     */
    void fillPostInfo(Post post);

    /**
     * 获取帖子浏览量
     * @param postId 帖子 id
     * @return 浏览量
     */
    long getViewCount(Long postId);

    /**
     * 获取帖子详情
     * @param id 帖子 id
     * @param loginUser 用户
     * @param request 请求
     * @return 帖子详情
     */
    Post getPostDetail(Long id, User loginUser, HttpServletRequest request);

}
