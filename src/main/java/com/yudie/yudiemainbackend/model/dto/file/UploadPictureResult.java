package com.yudie.yudiemainbackend.model.dto.file;

import lombok.Data;

/**
 * @description: 上传图片的结果
 * @author: xiaorui
 * @date: 2025-05-23 17:33
 **/
@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;
    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;

}
