package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserAuditRequest;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserJoinRequest;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.yudie.yudiemainbackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author xiaorui
* @description 针对表【space_user(空间用户关联表)】的数据库操作Service
* @createDate 2025-05-26 19:54:18
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员
     * @param spaceUserAddRequest 添加空间成员请求
     * @return 关联表 id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员
     * @param spaceUser 空间成员
     * @param add 是否为创建时检验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取空间成员包装类（单条）
     * @param spaceUser 空间成员
     * @param request 请求
     * @return 空间成员包装类
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类（列表）
     * @param spaceUserList 空间成员列表
     * @return 空间成员包装类列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 获取查询对象
     * @param spaceUserQueryRequest 空间成员查询请求
     * @return 查询对象
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 判断用户是否为空间成员
     * @param userId 用户 id
     * @param spaceId 空间 id
     * @return 是否为空间成员
     */
    boolean isSpaceMember(long userId, long spaceId);

    /**
     * 获取空间成员
     * @param spaceId 空间 id
     * @return 空间成员
     */
    List<User> getSpaceMembers(long spaceId);

    /**
     * 审核空间成员申请
     * @param spaceUserAuditRequest 审核请求
     * @param loginUser 当前登录用户
     * @return 是否审核成功
     */
    boolean auditSpaceUser(SpaceUserAuditRequest spaceUserAuditRequest, User loginUser);

    /**
     * 申请加入空间
     * @param spaceUserJoinRequest 申请请求
     * @param loginUser 当前登录用户
     * @return 是否申请成功
     */
    boolean joinSpace(SpaceUserJoinRequest spaceUserJoinRequest, User loginUser);

}
