package com.yudie.yudiemainbackend.esdao;

import com.yudie.yudiemainbackend.model.entity.es.EsUser;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @description: ES 用户数据访问层
 * @author: xiaorui
 * @date: 2025-05-30 20:13
 **/
@Repository
public interface EsUserDao extends ElasticsearchRepository<EsUser, Long> {

    /**
     * 根据用户账号查询
     * @param userAccount 用户账号
     * @return 用户信息
     */
    Optional<EsUser> findByUserAccount(String userAccount);

    /**
     * 根据用户名模糊查询
     * @param userName 用户名
     * @return 用户信息
     */
    List<EsUser> findByUserNameContaining(String userName);

    /**
     * 根据用户简介模糊查询
     * @param userProfile 用户简介
     * @return 用户信息
     */
    List<EsUser> findByUserProfileContaining(String userProfile);

    /**
     * 根据用户角色查询
     * @param userRole 用户角色
     * @return 用户信息
     */
    List<EsUser> findByUserRole(String userRole);

    /**
     * 根据用户名或简介模糊查询
     * @param userName 用户名
     * @param userProfile 用户简介
     * @return 用户信息
     */
    List<EsUser> findByUserNameContainingOrUserProfileContaining(String userName, String userProfile);

    /**
     * 根据用户账号模糊查询
     * @param userAccount 用户账号
     * @return 用户信息
     */
    List<EsUser> findByUserAccountContaining(String userAccount);
}

