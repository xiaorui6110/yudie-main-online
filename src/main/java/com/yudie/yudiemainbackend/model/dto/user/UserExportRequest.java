package com.yudie.yudiemainbackend.model.dto.user;

import lombok.Data;

import java.util.Date;

/**
 * @description: 用户数据导出请求
 * @author: xiaorui
 * @date: 2025-07-10 15:14
 **/
@Data
public class UserExportRequest {

    /**
     * 导出类型：1-天 2-周 3-月 4-年 5-自定义
     */
    private Integer type;

    /**
     * 自定义开始时间
     */
    private Date startTime;

    /**
     * 自定义结束时间
     */
    private Date endTime;

}
