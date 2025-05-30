package com.yudie.yudiemainbackend.esdao;

import com.yudie.yudiemainbackend.model.entity.es.EsSearchKeyword;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @description: ES 搜索关键词数据访问层
 * @author: siri
 * @date: 2025-05-30 20:15
 **/
@Repository
public interface EsSearchKeywordDao extends ElasticsearchRepository<EsSearchKeyword, String> {

    /**
     * 根据类型和关键词查询
     * @param type 类型
     * @param keyword 关键词
     * @return 搜索关键词
     */
    EsSearchKeyword findByTypeAndKeyword(String type, String keyword);

    /**
     * 根据类型查询热门关键词
     * @param type 类型
     * @param startTime 开始时间
     * @return 热门关键词
     */
    List<EsSearchKeyword> findByTypeAndUpdateTimeAfterOrderByCountDesc(String type, Date startTime);
}

