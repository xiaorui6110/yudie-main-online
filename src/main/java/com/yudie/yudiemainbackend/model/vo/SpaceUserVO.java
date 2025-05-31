package com.yudie.yudiemainbackend.model.vo;

import com.yudie.yudiemainbackend.model.entity.SpaceUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 空间成员响应类
 * @author: xiaorui
 * @date: 2025-05-26 19:59
 **/
@Data
public class SpaceUserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 空间信息
     */
    private SpaceVO space;

    /**
     * 审核状态：0-待审核 1-通过 2-拒绝
     */
    private Integer status;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     *
     * @param spaceUserVO 空间成员封装类
     * @return 空间成员对象
     */
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserVO, spaceUser);
        return spaceUser;
    }

    /**
     * 对象转封装类
     *
     * @param spaceUser 空间成员对象
     * @return 空间成员封装类
     */
    public static SpaceUserVO objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtils.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }
}
