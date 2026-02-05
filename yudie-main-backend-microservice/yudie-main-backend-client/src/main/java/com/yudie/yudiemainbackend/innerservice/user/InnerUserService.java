package com.yudie.yudiemainbackend.innerservice.user;

import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface InnerUserService {

    User getLoginUser(HttpServletRequest request);

    User isLogin(HttpServletRequest request);

    UserVO getUserVO(User user);

    boolean isAdmin(User user);

    User getById(Serializable id);

    List<User> listByIds(Collection<? extends Serializable> ids);

    boolean updateById(User user);

    UserVO getUserVOById(Long id);

}
