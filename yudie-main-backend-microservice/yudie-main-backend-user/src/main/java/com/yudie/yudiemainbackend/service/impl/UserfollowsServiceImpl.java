package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.innerservice.message.InnerPrivateChatService;
import com.yudie.yudiemainbackend.mapper.UserMapper;
import com.yudie.yudiemainbackend.mapper.UserfollowsMapper;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserFollowsAddRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserFollowsIsFollowsRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserfollowsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.entity.Userfollows;
import com.yudie.yudiemainbackend.model.vo.FollowersAndFansVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.UserService;
import com.yudie.yudiemainbackend.service.UserfollowsService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【userfollows(用户关注表)】的数据库操作Service实现
* @createDate 2025-05-28 20:08:30
*/
@Service
public class UserfollowsServiceImpl extends ServiceImpl<UserfollowsMapper, Userfollows>
    implements UserfollowsService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private InnerPrivateChatService privateChatService;

    /**
     * 添加关注（0-取消关注操作，1-关注操作）
     * @param userFollowsAddRequest 用户关注请求
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addUserFollows(UserFollowsAddRequest userFollowsAddRequest) {
        // 1. 校验参数
        Long followerId = userFollowsAddRequest.getFollowerId();
        Long followingId = userFollowsAddRequest.getFollowingId();
        Integer followStatus = userFollowsAddRequest.getFollowStatus();
        ThrowUtils.throwIf(followerId == null || followingId == null || followStatus == null,
                ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(followStatus != 0 && followStatus != 1,
                ErrorCode.PARAMS_ERROR, "关注状态错误");
        // 2. 查询是否关注
        QueryWrapper<Userfollows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", followerId)
                .eq("followingId", followingId)
                .eq("isDelete", 0);
        Userfollows userfollows = this.getOne(queryWrapper);
        if (followStatus == 1) {
            // 3. 添加关注
            if (userfollows == null) {
                userfollows = new Userfollows();
                userfollows.setFollowerId(followerId);
                userfollows.setFollowingId(followingId);
                userfollows.setFollowStatus(1);
                userfollows.setCreateTime(new Date());
            }
            userfollows.setFollowStatus(1);
            // 检查是否存在反向关注
            QueryWrapper<Userfollows> reverseQuery = new QueryWrapper<>();
            reverseQuery.eq("followerId", followingId)
                    .eq("followingId", followerId)
                    .eq("followStatus", 1)
                    .eq("isDelete", 0);

            boolean isMutual = this.count(reverseQuery) > 0;
            userfollows.setIsMutual(isMutual ? 1 : 0);
            // 如果形成了双向关注，更新两条记录的互关状态
            if (isMutual) {
                this.update()
                        .set("isMutual", 1)
                        .eq("followerId", followingId)
                        .eq("followingId", followerId)
                        .eq("followStatus", 1)
                        .eq("isDelete", 0)
                        .update();
                // 更新私聊类型为好友
                privateChatService.updateChatType(followerId, followingId, true);
            }
        } else {
            // 4. 取消关注
            if (userfollows != null) {
                userfollows.setFollowStatus(0);
                userfollows.setIsMutual(0);
                // 更新对方的互关状态
                this.update()
                        .set("isMutual", 0)
                        .eq("followerId", followingId)
                        .eq("followingId", followerId)
                        .eq("followStatus", 1)
                        .eq("isDelete", 0)
                        .update();
                // 更新私聊类型为普通私信
                privateChatService.updateChatType(followerId, followingId, false);

            }
        }
        userfollows.setEditTime(new Date());
        userfollows.setLastInteractionTime(new Date());
        return this.saveOrUpdate(userfollows);

    }

    /**
     * 获取关注列表
     * @param userfollowsQueryRequest 用户获取关注列表请求
     * @return 关注列表
     */
    @Override
    public Page<UserVO> getFollowOrFanList(UserfollowsQueryRequest userfollowsQueryRequest) {
        // 1. 校验参数
        ThrowUtils.throwIf(userfollowsQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long followerId = userfollowsQueryRequest.getFollowerId();
        Long followingId = userfollowsQueryRequest.getFollowingId();
        int searchType = userfollowsQueryRequest.getSearchType();
        long current = userfollowsQueryRequest.getCurrent();
        long pageSize = userfollowsQueryRequest.getPageSize();
        if (current <= 0) {
            current = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        // 2. 查询0-关注或1-粉丝列表
        QueryWrapper<Userfollows> queryWrapper = getUserfollowsQueryWrapper(userfollowsQueryRequest);
        IPage<Userfollows> userfollowsPage = this.page(new Page<>(current, pageSize), queryWrapper);
        List<Userfollows> userfollowsList = userfollowsPage.getRecords();
        if (userfollowsList == null || userfollowsList.isEmpty()) {
            return new Page<>();
        }
        List<Long> targetIdList;
        if (followerId!= null && searchType == 0) {
            targetIdList = userfollowsList.stream()
                    .map(Userfollows::getFollowingId)
                    .collect(Collectors.toList());
        } else if (followingId!= null && searchType == 1) {
            targetIdList = userfollowsList.stream()
                    .map(Userfollows::getFollowerId)
                    .collect(Collectors.toList());
        } else {
            return new Page<>();
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", targetIdList);
        List<User> userList = userMapper.selectList(userQueryWrapper);
        if (userList == null || userList.isEmpty()) {
            return new Page<>();
        }
        List<UserVO> userVOList = userService.getUserVOList(userList);
        // 将分页信息设置到 Page 对象中
        Page<UserVO> page = new Page<>(current, pageSize);
        page.setRecords(userVOList);
        page.setTotal(userfollowsPage.getTotal());
        return page;
    }

    /**
     * 获取关注列表查询条件
     * @param userfollowsQueryRequest 用户获取关注列表请求
     * @return 关注列表查询条件
     */
    private QueryWrapper<Userfollows> getUserfollowsQueryWrapper(UserfollowsQueryRequest userfollowsQueryRequest) {
        QueryWrapper<Userfollows> queryWrapper = new QueryWrapper<>();
        Long followerId = userfollowsQueryRequest.getFollowerId();
        Long followingId = userfollowsQueryRequest.getFollowingId();
        if (followerId!= null) {
            queryWrapper.eq("followerId", followerId);
        }
        if (followingId!= null) {
            queryWrapper.eq("followingId", followingId);
        }
        queryWrapper.eq("followStatus", 1);
        queryWrapper.orderByDesc("lastInteractionTime");
        return queryWrapper;
    }

    /**
     * 判断是否关注
     * @param userFollowsIsFollowsRequest 用户判断是否关注请求
     * @return 是否成功
     */
    @Override
    public Boolean findIsFollow(UserFollowsIsFollowsRequest userFollowsIsFollowsRequest) {
        long followerId = userFollowsIsFollowsRequest.getFollowerId();
        long followingId = userFollowsIsFollowsRequest.getFollowingId();
        ThrowUtils.throwIf(followerId <= 0 || followingId <= 0, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Userfollows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", followerId);
        queryWrapper.eq("followingId", followingId);
        queryWrapper.eq("followStatus", 1);
        return this.getOne(queryWrapper) != null;
    }

    /**
     * 获取粉丝列表
     * @param id 用户 id
     * @return 粉丝列表
     */
    @Override
    public List<Long> getFollowList(Long id) {
        if (id== null) {
            return null;
        }
        QueryWrapper<Userfollows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", id);
        queryWrapper.eq("followStatus", 1);
        List<Userfollows> userfollowsList = this.list(queryWrapper);
        return userfollowsList.stream().map(Userfollows::getFollowingId).collect(Collectors.toList());
    }

    /**
     * 获取关注数和粉丝数
     * @param id 用户 id
     * @return 关注数和粉丝数
     */
    @Override
    public FollowersAndFansVO getFollowAndFansCount(Long id) {
        QueryWrapper<Userfollows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", id);
        queryWrapper.eq("followStatus", 1);
        long followCount = this.count(queryWrapper);
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followingId", id);
        queryWrapper.eq("followStatus", 1);
        long fansCount = this.count(queryWrapper);
        FollowersAndFansVO followersAndFansVO = new FollowersAndFansVO();
        followersAndFansVO.setFollowCount(followCount);
        followersAndFansVO.setFansCount(fansCount);
        return followersAndFansVO;
    }

}

