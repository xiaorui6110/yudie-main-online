package com.yudie.yudiemainbackend.model.dto.picture;

import com.yudie.yudiemainbackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 创建扩图任务请求
 * @author: xiaorui
 * @date: 2025-05-23 21:42
 **/
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;

}
