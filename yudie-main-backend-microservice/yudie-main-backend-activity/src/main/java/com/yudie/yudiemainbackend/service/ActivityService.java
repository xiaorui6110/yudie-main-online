package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.dto.activity.ActivityAddRequest;
import com.yudie.yudiemainbackend.model.dto.activity.ActivityQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Activity;
import com.yudie.yudiemainbackend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author xiaorui
* @description 针对表【activity(活动表)】的数据库操作Service
* @createDate 2025-07-10 20:51:00
*/
public interface ActivityService extends IService<Activity> {

    /**
     * 创建活动（仅管理员）
     * @param activityAddRequest 创建活动请求
     * @param loginUser 用户
     * @return 活动ID
     */
    Long addActivity(ActivityAddRequest activityAddRequest, User loginUser);

    /**
     * 审核活动（仅管理员）
     * @param activityId 活动ID
     * @param status 状态
     * @param reviewMessage 审核信息
     * @param loginUser 用户
     */
    void reviewActivity(Long activityId, Integer status, String reviewMessage, User loginUser);

    /**
     * 分页获取活动列表
     * @param activityQueryRequest 活动查询请求
     * @param loginUser 用户
     * @return 活动列表
     */
    Page<Activity> listActivities(ActivityQueryRequest activityQueryRequest, User loginUser);

    /**
     * 获取活动详情（带浏览量统计）
     * @param activityId 活动ID
     * @param loginUser 用户
     * @param request 请求
     * @return 活动详情
     */
    Activity getActivityDetail(Long activityId, User loginUser, HttpServletRequest request);

    /**
     * 获取活动浏览量
     * @param activityId 活动ID
     * @return 浏览量
     */
    long getViewCount(Long activityId);

    /**
     * 填充活动信息（用户、附件等）
     * @param activity 活动
     */
    void fillActivityInfo(Activity activity);

    /**
     * 获取轮播图活动列表（未过期且已审核通过的）
     * @param activityQueryRequest 活动查询请求
     * @return 轮播图活动列表
     */
    Page<Activity> listCarouselActivities(ActivityQueryRequest activityQueryRequest);

}
