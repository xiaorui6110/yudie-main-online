package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserFollowsAddRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserFollowsIsFollowsRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserfollowsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Userfollows;
import com.yudie.yudiemainbackend.model.vo.FollowersAndFansVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【userfollows(用户关注表)】的数据库操作Service
* @createDate 2025-05-28 20:08:30
*/
public interface UserfollowsService extends IService<Userfollows> {

    /**
     * 添加关注
     * @param userFollowsAddRequest 用户关注请求
     * @return 是否成功
     */
    Boolean addUserFollows(UserFollowsAddRequest userFollowsAddRequest);

    /**
     * 获取关注列表
     * @param userfollowsQueryRequest 用户获取关注列表请求
     * @return 关注列表
     */
    Page<UserVO> getFollowOrFanList(UserfollowsQueryRequest userfollowsQueryRequest);

    /**
     * 判断是否关注
     * @param userFollowsIsFollowsRequest 用户判断是否关注请求
     * @return 是否成功
     */
    Boolean findIsFollow(UserFollowsIsFollowsRequest userFollowsIsFollowsRequest);

    /**
     * 获取粉丝列表
     * @param id 用户 id
     * @return 粉丝列表
     */
    List<Long> getFollowList(Long id);

    /**
     * 获取关注数和粉丝数
     * @param id 用户 id
     * @return 关注数和粉丝数
     */
    FollowersAndFansVO getFollowAndFansCount(Long id);

}
