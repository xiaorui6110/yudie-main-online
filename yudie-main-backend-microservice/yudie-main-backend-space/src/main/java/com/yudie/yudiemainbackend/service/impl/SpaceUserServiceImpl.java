package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserService;
import com.yudie.yudiemainbackend.mapper.SpaceUserMapper;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserAuditRequest;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserJoinRequest;
import com.yudie.yudiemainbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Space;
import com.yudie.yudiemainbackend.model.entity.SpaceUser;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.enums.OperationEnum;
import com.yudie.yudiemainbackend.model.enums.SpaceRoleEnum;
import com.yudie.yudiemainbackend.model.vo.SpaceUserVO;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.SpaceService;
import com.yudie.yudiemainbackend.service.SpaceUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【space_user(空间用户关联表)】的数据库操作Service实现
* @createDate 2025-05-26 19:54:18
*/
@DubboService
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    private InnerUserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    /**
     * 添加空间成员
     * @param spaceUserAddRequest 添加空间成员请求
     * @return 关联表 id
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        // 设置初始状态为通过，因为只有管理员可以使用添加方法
        spaceUser.setStatus(OperationEnum.APPROVE.getValue());
        validSpaceUser(spaceUser, true);
        // 校验空间成员数量是否已达到上限
        long memberCount = this.count(new QueryWrapper<SpaceUser>()
                .eq("spaceId", spaceUserAddRequest.getSpaceId())
                // 只统计审核已通过的成员
                .eq("status", OperationEnum.APPROVE.getValue()));
        ThrowUtils.throwIf(memberCount >= 50, ErrorCode.OPERATION_ERROR, "该空间成员数量已达到上限");
        // 数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    /**
     * 校验空间成员
     * @param spaceUser 空间成员
     * @param add 是否为创建时检验
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时，空间 id 和用户 id 必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    /**
     * 获取空间成员包装类（单条）
     * @param spaceUser 空间成员
     * @param request 请求
     * @return 空间成员包装类
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        // 设置状态
        spaceUserVO.setStatus(spaceUser.getStatus());
        return spaceUserVO;
    }

    /**
     * 获取空间成员包装类（列表）
     * @param spaceUserList 空间成员列表
     * @return 空间成员包装类列表
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 1. 校验参数
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 转换为封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        // 2. 查询关联查询的用户 id 和空间 id
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 3. 批量查询用户和空间
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));
        // 4. 填充 SpaceUserVO 的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }

    /**
     * 获取查询对象
     * @param spaceUserQueryRequest 空间成员查询请求
     * @return 查询对象
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        Integer status = spaceUserQueryRequest.getStatus();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), "status", status);
        return queryWrapper;
    }

    /**
     * 判断用户是否为空间成员
     * @param userId 用户 id
     * @param spaceId 空间 id
     * @return 是否为空间成员
     */
    @Override
    public boolean isSpaceMember(long userId, long spaceId) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("spaceId", spaceId)
                // 只有审核通过的才算是成员
                .eq("status", OperationEnum.APPROVE.getValue());
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<User> getSpaceMembers(long spaceId) {
        // 1. 查询空间的所有成员
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spaceId", spaceId)
                // 只获取审核通过的成员
                .eq("status", OperationEnum.APPROVE.getValue());
        List<SpaceUser> spaceUsers = this.list(queryWrapper);
        if (CollUtil.isEmpty(spaceUsers)) {
            return Collections.emptyList();
        }
        // 2. 获取所有用户ID
        Set<Long> userIds = spaceUsers.stream()
                .map(SpaceUser::getUserId)
                .collect(Collectors.toSet());
        // 3. 批量查询用户信息并脱敏
        List<User> users = userService.listByIds(userIds);
        // 4. 对用户信息进行脱敏处理，保留更多字段
        return users.stream()
                .map(user -> {
                    User safetyUser = new User();
                    safetyUser.setId(user.getId());
                    safetyUser.setUserAccount(user.getUserAccount());
                    safetyUser.setUserName(user.getUserName());
                    safetyUser.setUserAvatar(user.getUserAvatar());
                    safetyUser.setUserProfile(user.getUserProfile());
                    safetyUser.setUserRole(user.getUserRole());
                    safetyUser.setCreateTime(user.getCreateTime());
                    return safetyUser;
                })
                .collect(Collectors.toList());
    }

    /**
     * 审核空间成员申请
     * @param spaceUserAuditRequest 审核请求
     * @param loginUser 当前登录用户
     * @return 是否审核成功
     */
    @Override
    public boolean auditSpaceUser(SpaceUserAuditRequest spaceUserAuditRequest, User loginUser) {
        // 校验参数
        if (spaceUserAuditRequest == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long spaceId = spaceUserAuditRequest.getSpaceId();
        Long userId = spaceUserAuditRequest.getUserId();
        Integer status = spaceUserAuditRequest.getStatus();

        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId, status),
                ErrorCode.PARAMS_ERROR);
        // 校验状态值
        if (status != OperationEnum.APPROVE.getValue() && status != OperationEnum.REJECT.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核状态不合法");
        }
        // 校验当前用户是否是该空间的管理员
        QueryWrapper<SpaceUser> adminQuery = new QueryWrapper<>();
        adminQuery.eq("spaceId", spaceId)
                .eq("userId", loginUser.getId())
                .eq("spaceRole", "admin");
        SpaceUser adminUser = this.getOne(adminQuery);
        ThrowUtils.throwIf(adminUser == null, ErrorCode.NOT_AUTH_ERROR, "您不是该空间的管理员");
        // 校验被审核用户是否存在申请记录
        QueryWrapper<SpaceUser> userQuery = new QueryWrapper<>();
        userQuery.eq("spaceId", spaceId)
                .eq("userId", userId);
        SpaceUser targetUser = this.getOne(userQuery);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "未找到该用户的申请记录");
        // 校验被审核用户不是管理员
        ThrowUtils.throwIf("admin".equals(targetUser.getSpaceRole()),
                ErrorCode.OPERATION_ERROR, "不能审核管理员");
        // 如果是通过申请，需要检查成员数量
        if (status == OperationEnum.APPROVE.getValue()) {
            long memberCount = this.count(new QueryWrapper<SpaceUser>()
                    .eq("spaceId", spaceId)
                    .eq("status", OperationEnum.APPROVE.getValue()));
            ThrowUtils.throwIf(memberCount >= 50, ErrorCode.OPERATION_ERROR, "该空间成员数量已达到上限");
        }
        // 更新审核状态,保持原有角色不变
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setId(targetUser.getId());
        spaceUser.setStatus(status);
        return this.updateById(spaceUser);
    }

    /**
     * 申请加入空间
     * @param spaceUserJoinRequest 申请请求
     * @param loginUser 当前登录用户
     * @return 是否申请成功
     */
    @Override
    public boolean joinSpace(SpaceUserJoinRequest spaceUserJoinRequest, User loginUser) {
        if (spaceUserJoinRequest == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long spaceId = spaceUserJoinRequest.getSpaceId();
        Long userId = loginUser.getId();
        // 校验空间是否存在
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 校验空间成员数量是否已达到上限
        long memberCount = this.count(new QueryWrapper<SpaceUser>()
                .eq("spaceId", spaceId)
                .eq("status", 1));
        ThrowUtils.throwIf(memberCount >= 50, ErrorCode.OPERATION_ERROR, "该空间成员数量已达到上限");
        // 校验用户是否已经是成员
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spaceId", spaceId)
                .eq("userId", userId);
        SpaceUser existSpaceUser = this.getOne(queryWrapper);
        if (existSpaceUser != null) {
            if (existSpaceUser.getStatus() == OperationEnum.APPROVE.getValue()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已是该空间成员");
            }
            if (existSpaceUser.getStatus() == OperationEnum.DELETE.getValue()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "您的申请正在审核中");
            }
        }
        // 创建申请记录
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        // 设置为待审核状态
        spaceUser.setStatus(OperationEnum.DELETE.getValue());
        // 默认设置为查看者角色
        spaceUser.setSpaceRole("viewer");
        return this.save(spaceUser);
    }

}




