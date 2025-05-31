package com.yudie.yudiemainbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 留言板VO
 * @author: siri
 * @date: 2025-05-31 10:42
 **/
@Data
public class MessageVO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

}
