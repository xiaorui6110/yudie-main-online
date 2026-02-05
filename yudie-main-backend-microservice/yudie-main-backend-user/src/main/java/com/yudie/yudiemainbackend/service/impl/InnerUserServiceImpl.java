package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.yudie.yudiemainbackend.constant.UserConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserService;
import com.yudie.yudiemainbackend.manager.auth.StpKit;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.enums.UserRoleEnum;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public User getLoginUser(HttpServletRequest request) {
        try {
            // 优先从 Sa-Token 中获取登录信息
            if (StpKit.SPACE.isLogin()) {
                User user = (User) StpKit.SPACE.getSession().get(UserConstant.USER_LOGIN_STATE);
                if (user != null) {
                    return user;
                }
            }
            // 1.从 Session 中获取登录信息
            Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
            User currentUser = (User) userObj;
            if (currentUser == null || currentUser.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 2.查询数据库获取用户信息
            long userId = currentUser.getId();
            currentUser = this.getById(userId);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 更新 Sa-Token 中的用户信息
            StpKit.SPACE.login(userId);
            StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, currentUser);
            // 3.返回用户信息
            return currentUser;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    @Override
    public User isLogin(HttpServletRequest request) {
        // 1.从 Session 中获取登录信息
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 2.查询数据库获取用户信息
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            return null;
        }
        return currentUser;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN_ROLE_ENUM.getValue().equals(user.getUserRole());
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public boolean updateById(User user) {
        return userService.updateById(user);
    }

    @Override
    public UserVO getUserVOById(Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return null;
        }
        return getUserVO(user);
    }
}
