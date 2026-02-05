package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.dto.space.SpaceAddRequest;
import com.yudie.yudiemainbackend.model.dto.space.SpaceQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author xiaorui
* @description 针对表【space(空间表)】的数据库操作Service
* @createDate 2025-05-26 19:52:02
*/
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     * @param spaceAddRequest 创建空间请求
     * @param loginUser 当前登录用户
     * @return 空间 id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间
     * @param space 空间
     * @param add 是否为创建时检验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类（单条）
     * @param space 空间
     * @param request 请求
     * @return 空间包装类
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类（分页）
     * @param spacePage 页数
     * @param request 请求
     * @return 空间包装类列表
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询对象
     * @param spaceQueryRequest 空间查询请求
     * @return 空间查询对象
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     * @param loginUser 当前登录用户
     * @param space 空间
     */
    void checkSpaceAuth(User loginUser, Space space);

    /**
     * 根据空间 id 获取空间VO
     * @param id 空间 id
     * @return 空间VO
     */
    SpaceVO getSpaceVOById(long id, HttpServletRequest request);


}
