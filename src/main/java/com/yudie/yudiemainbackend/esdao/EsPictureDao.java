package com.yudie.yudiemainbackend.esdao;

import com.yudie.yudiemainbackend.model.entity.es.EsPicture;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description: ES 图片数据访问层
 * @author: siri
 * @date: 2025-05-30 20:14
 **/
@Repository
public interface EsPictureDao extends ElasticsearchRepository<EsPicture, Long> {

    /**
     * 根据分类查询图片
     * @param category 分类
     * @return 图片列表
     */
    List<EsPicture> findByCategory(String category);

    /**
     * 根据用户ID查询图片
     * @param userId 用户ID
     * @return 图片列表
     */
    List<EsPicture> findByUserId(Long userId);

    /**
     * 根据审核状态查询图片
     * @param reviewStatus 审核状态
     * @return 图片列表
     */
    List<EsPicture> findByReviewStatus(Integer reviewStatus);

    /**
     * 根据名称或简介模糊查询
     * @param name 名称
     * @param introduction 简介
     * @return 图片列表
     */
    List<EsPicture> findByNameContainingOrIntroductionContaining(String name, String introduction);

    /**
     * 根据标签模糊查询
     * @param tag 标签
     * @return 图片列表
     */
    List<EsPicture> findByTagsContaining(String tag);

    /**
     * 根据图片格式查询
     * @param picFormat 图片格式
     * @return 图片列表
     */
    List<EsPicture> findByPicFormat(String picFormat);
}
