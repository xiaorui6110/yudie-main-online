package com.yudie.yudiemainbackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.constant.CrawlerConstant;
import com.yudie.yudiemainbackend.constant.RedisConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.model.dto.like.LikeRequest;
import com.yudie.yudiemainbackend.model.dto.post.PostAddRequest;
import com.yudie.yudiemainbackend.model.dto.post.PostQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.LikeRecordService;
import com.yudie.yudiemainbackend.service.PostService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description: 帖子实现类接口
 * @author: xiaorui
 * @date: 2025-05-29 21:44
 **/
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private UserService userService;;

    @Resource
    private PostService postService;

    @Resource
    private CrawlerManager crawlerManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LikeRecordService likeRecordService;

    /**
     * 发布帖子
     * @param postAddRequest 发布帖子请求
     * @param request 请求
     * @return 帖子 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long postId = postService.addPost(postAddRequest, loginUser);
        return ResultUtils.success(postId);
    }

    /**
     * 更新帖子
     * @param post 帖子
     * @param request 请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@PathVariable Post post, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        Post oldPost = postService.getById(post.getId());
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        boolean result = postService.updatePost(post);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取帖子详情
     * @param id 帖子 id
     * @param request 请求
     * @return 帖子详情
     */
    @GetMapping("/get/{id}")
    public BaseResponse<Post> getPostById(@PathVariable Long id, HttpServletRequest request) {
        crawlerManager.detectNormalRequest(request);
        User loginUser = userService.getLoginUser(request);
        Post post = postService.getPostDetail(id, loginUser, request);
        return ResultUtils.success(post);
    }

    /**
     * 分页获取帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @param request 请求
     * @return 帖子列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest, HttpServletRequest request) {
        User loginUser = userService.isLogin(request);
        if (loginUser != null) {;
            ThrowUtils.throwIf(loginUser.getUserRole().equals(CrawlerConstant.BAN_ROLE),
                    ErrorCode.NOT_AUTH_ERROR, "封禁用户禁止获取数据，请联系管理员");
        }
        long size = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        crawlerManager.detectNormalRequest(request);
        Page<Post> postPage = postService.getPostList(postQueryRequest, loginUser);
        return ResultUtils.success(postPage);
    }

    /**
     * 删除帖子
     * @param id 帖子 id
     * @param request 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> deletePost(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Post post = postService.getById(id);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        if (!post.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        boolean result = postService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 点赞/取消点赞
     * @param id 帖子 id
     * @param request 请求
     * @return 是否点赞成功
     */
    @PostMapping("/like/{id}")
    public BaseResponse<Boolean> likePost(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        LikeRequest likeRequest = new LikeRequest();
        likeRequest.setTargetId(id);
        likeRequest.setTargetType(2);
        likeRequest.setIsLiked(true);
        likeRecordService.doLike(likeRequest, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 审核帖子
     * @param id 帖子 id
     * @param status 审核状态
     * @param message 审核信息
     * @param request 请求
     * @return 是否审核成功
     */
    @PostMapping("/review/{id}")
    public BaseResponse<Boolean> reviewPost(@PathVariable Long id, @RequestParam Integer status,
                                            @RequestParam(required = false) String message, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        postService.reviewPost(id, status, message, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取我的帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @param request 请求
     * @return 我的帖子列表
     */
    @PostMapping("/my/list")
    public BaseResponse<Page<Post>> listMyPosts(@RequestBody PostQueryRequest postQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        postQueryRequest.setUserId(loginUser.getId());
        Page<Post> postPage = postService.getMyPostList(postQueryRequest);
        return ResultUtils.success(postPage);
    }

    /**
     * 获取关注用户的帖子列表
     * @param postQueryRequest 获取帖子列表请求
     * @param request 请求
     * @return 关注用户的帖子列表
     */
    @PostMapping("/follow")
    public BaseResponse<Page<Post>> getFollowPosts(@RequestBody PostQueryRequest postQueryRequest, HttpServletRequest request) {
        crawlerManager.detectNormalRequest(request);
        return ResultUtils.success(postService.getFollowPosts(postQueryRequest, request));
    }

    /**
     * 获取帖子榜单
     * @param id 帖子 id
     * @param request 请求
     * @return 帖子榜单
     */
    @GetMapping("/top100/{id}")
    public BaseResponse<List<Post>> getTop100Post(@PathVariable Long id, HttpServletRequest request) {
        crawlerManager.detectNormalRequest(request);
        // 构建 Redis 缓存的 key
        String cacheKey = RedisConstant.TOP_100_POST_REDIS_KEY_PREFIX + id;
        // 先从 Redis 缓存中获取数据
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            List<Post> postList = JSONUtil.toList(cachedValue, Post.class);
            return ResultUtils.success(postList);
        }
        // 缓存未命中，调用服务层方法获取数据
        List<Post> postList = postService.getTop100Post(id);
        // 设置缓存，添加随机过期时间防止缓存雪崩
        int cacheExpireTime = (int) (RedisConstant.TOP_100_POST_REDIS_KEY_EXPIRE_TIME
                + RandomUtil.randomInt(0, 6000));
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(postList),
                cacheExpireTime, TimeUnit.SECONDS);
        return ResultUtils.success(postList);
    }

}
