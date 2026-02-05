package com.yudie.yudiemainbackend.job;

import com.yudie.yudiemainbackend.model.entity.HotSearch;

import java.util.List;

/**
 * @description: 热门搜索同步接口
 * @author: xiaorui
 * @date: 2025-05-31 16:16
 **/
public interface HotSearchSync {

    /**
     * 更新Redis缓存
     */
    void updateCache(String type, List<HotSearch> hotSearchList);

    /**
     * 预热缓存
     */
    void warmUpCache();

}