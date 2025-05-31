package com.yudie.yudiemainbackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 空间使用排行分析请求（仅管理员）
 * @author: xiaorui
 * @date: 2025-05-28 08:21
 **/
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}

