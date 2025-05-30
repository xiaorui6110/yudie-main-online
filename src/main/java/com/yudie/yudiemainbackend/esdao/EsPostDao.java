package com.yudie.yudiemainbackend.esdao;

import com.yudie.yudiemainbackend.model.entity.es.EsPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: ES 文章数据访问层
 * @author: siri
 * @date: 2025-05-30 20:14
 **/
@Repository
public interface EsPostDao extends ElasticsearchRepository<EsPost, Long> {
}

