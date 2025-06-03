package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.dto.space.SpaceAddRequest;
import com.yudie.yudiemainbackend.model.dto.space.SpaceQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Space;
import com.yudie.yudiemainbackend.model.entity.SpaceUser;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.enums.OperationEnum;
import com.yudie.yudiemainbackend.model.enums.SpaceLevelEnum;
import com.yudie.yudiemainbackend.model.enums.SpaceRoleEnum;
import com.yudie.yudiemainbackend.model.enums.SpaceTypeEnum;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.SpaceService;
import com.yudie.yudiemainbackend.mapper.SpaceMapper;
import com.yudie.yudiemainbackend.service.SpaceUserService;
import com.yudie.yudiemainbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【space(空间表)】的数据库操作Service实现
* @createDate 2025-05-26 19:52:02
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService{

    private static final int MAX_SPACE_NAME_LENGTH = 30;

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 创建空间
     * @param spaceAddRequest 创建空间请求
     * @param loginUser 当前登录用户
     * @return 空间 id
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1.转换实体类和 DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        // 2. 校验参数（传入的是实体类）
        this.validSpace(space, true);
        // 3. 校验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser) ) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        // 4. 控制用户创建空间的数量
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断是否已有私有空间
                if (SpaceTypeEnum.PRIVATE.getValue() == space.getSpaceType()) {
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, SpaceTypeEnum.PRIVATE.getValue())
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个私有空间");
                }
                // 判断团队空间数量是否超限
                else if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                    long count = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                            .count();
                    ThrowUtils.throwIf(count >= 10, ErrorCode.OPERATION_ERROR, "每个用户最多只能创建 10 个团队空间");
                }
                // 创建空间
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                // 创建成功后，如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(space.getUserId());
                    // 设置为管理员权限
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    // 设置为已通过状态
                    spaceUser.setStatus(OperationEnum.APPROVE.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队空间成员记录失败");
                }
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    /**
     * 校验空间参数
     * @param space 空间
     * @param add 是否为创建时检验
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 创建时校验
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不能为空");
            }
        }
        // 修改数据时，空间名称进行校验
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > MAX_SPACE_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        // 修改数据时，空间级别进行校验
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        // 修改数据时，空间类别进行校验
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
        }
    }

    /**
     * 获取空间包装类（单条）
     * @param space 空间
     * @param request 请求
     * @return 空间包装类
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 获取空间包装类（分页）
     * @param spacePage 页数
     * @param request 请求
     * @return 空间包装类列表
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        // 1. 获取分页参数
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>();
        spaceVOPage.setCurrent(spacePage.getCurrent());
        spaceVOPage.setSize(spacePage.getSize());
        spaceVOPage.setTotal(spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 2. 对象列表转换为封装类列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 3. 关联查询用户信息
        Set<Long> userIdSet = spaceVOList.stream()
                .map(SpaceVO::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 4. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId) ) {;
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 获取查询对象（分页）
     * @param spaceQueryRequest 空间查询请求
     * @return 查询对象
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 根据空间级别填充空间对象
     * @param space 空间
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 校验空间权限
     * @param loginUser 当前登录用户
     * @param space 空间
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可编辑
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
    }

    /**
     * 根据空间 id 获取空间VO
     * @param id 空间 id
     * @return 空间VO
     */
    @Override
    public SpaceVO getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space  = this.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取图片VO
        return this.getSpaceVO(space, request);
    }
}




