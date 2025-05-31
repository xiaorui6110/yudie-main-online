package com.yudie.yudiemainbackend.model.dto.post;

import lombok.Data;

/**
 * @description: 上传帖子附件请求
 * @author: xiaorui
 * @date: 2025-05-29 15:46
 **/
@Data
public class PostAttachmentRequest {
    /**
     *  1-图片 2-文件
     */
    private Integer type;

    /**
     *  附件的 url
     */
    private String url;

    /**
     *  附件的名称
     */
    private String name;

    /**
     *  附件的大小
     */
    private Long size;

    /**
     *  附件的排序
     */
    private Integer sort;
}
