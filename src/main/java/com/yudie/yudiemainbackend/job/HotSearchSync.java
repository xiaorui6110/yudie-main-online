package com.yudie.yudiemainbackend.job;

/**
 * @description: 热门搜索同步接口
 * @author: xiaorui
 * @date: 2025-05-31 16:16
 **/
public interface HotSearchSync {

    /**
     * 同步热门搜索数据
     */
    void syncHotSearch();

    /**
     * 预热缓存
     */
    void warmUpCache();

}