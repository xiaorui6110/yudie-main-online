package com.yudie.yudiemainbackend.esdao;

import com.yudie.yudiemainbackend.model.entity.es.EsSpace;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: ES 空间数据访问层
 * @author: xiaorui
 * @date: 2025-05-30 20:14
 **/
@Repository
public interface EsSpaceDao extends ElasticsearchRepository<EsSpace, Long> {
}

