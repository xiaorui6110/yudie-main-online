package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.annotation.AuthCheck;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.dto.activity.ActivityAddRequest;
import com.yudie.yudiemainbackend.model.dto.activity.ActivityQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Activity;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.ActivityService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @description: 活动实现类接口
 * @author: xiaorui
 * @date: 2025-07-10 20:54
 **/
@Slf4j
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    /**
     * 创建活动（仅管理员）
     * @param activityAddRequest 创建活动请求
     * @param request 请求
     * @return 活动ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addActivity(@RequestBody ActivityAddRequest activityAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(activityAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long activityId = activityService.addActivity(activityAddRequest, loginUser);
        return ResultUtils.success(activityId);
    }

    /**
     * 审核活动（仅管理员）
     * @param activityId 活动ID
     * @param status 状态
     * @param reviewMessage 审核信息
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> reviewActivity(@RequestParam Long activityId,
                                                @RequestParam Integer status,
                                                @RequestParam(required = false) String reviewMessage,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(activityId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(status == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        activityService.reviewActivity(activityId, status, reviewMessage, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取活动列表
     * @param activityQueryRequest 活动查询请求
     * @param request 请求
     * @return 活动列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Activity>> listActivityByPage(@RequestBody ActivityQueryRequest activityQueryRequest,
                                                           HttpServletRequest request) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<Activity> activityPage = activityService.listActivities(activityQueryRequest, loginUser);
        return ResultUtils.success(activityPage);
    }

    /**
     * 根据 id 获取活动详情
     * @param id 活动ID
     * @param request 请求
     * @return 活动详情
     */
    @GetMapping("/get")
    public BaseResponse<Activity> getActivityById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.isLogin(request);
        Activity activity = activityService.getActivityDetail(id, loginUser, request);
        return ResultUtils.success(activity);
    }

    /**
     * 获取轮播图活动列表
     * @param activityQueryRequest 活动查询请求
     * @return 活动列表
     */
    @PostMapping("/list/carousel")
    public BaseResponse<Page<Activity>> listCarouselActivities(@RequestBody ActivityQueryRequest activityQueryRequest) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<Activity> activityPage = activityService.listCarouselActivities(activityQueryRequest);
        return ResultUtils.success(activityPage);
    }

    /**
     * 删除活动（仅管理员）
     * @param id 活动ID
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteActivity(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 验证用户是否登录（虽然没有使用 loginUser）
        userService.getLoginUser(request);
        activityService.removeById(id);
        return ResultUtils.success(true);
    }

}
