package com.yudie.yudiemainbackend.service;

import com.yudie.yudiemainbackend.model.dto.search.SearchRequest;
import com.yudie.yudiemainbackend.model.entity.HotSearch;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.data.domain.Page;

import java.util.List;

/**
* @author lenovo
* @description 针对表【hot_search(热门搜索记录表)】的数据库操作Service
* @createDate 2025-05-30 09:53:07
*/
public interface SearchService extends IService<HotSearch> {

    /**
     * 获取热门搜索关键词
     * @param type 搜索类型
     * @param size 搜索关键词数量
     * @return 热门搜索关键词列表
     */
    List<String> getHotSearchKeywords(String type, Integer size);

    /**
     * 统一搜索接口
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    Page<?> doSearch(SearchRequest searchRequest);

}
