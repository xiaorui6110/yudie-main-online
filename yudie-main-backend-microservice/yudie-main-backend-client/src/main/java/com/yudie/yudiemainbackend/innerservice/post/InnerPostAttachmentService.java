package com.yudie.yudiemainbackend.innerservice.post;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.entity.PostAttachment;

import java.util.List;

public interface InnerPostAttachmentService {


    boolean saveBatch(List<PostAttachment> postAttachments);

    List<PostAttachment> list(QueryWrapper<PostAttachment> queryWrapper);

    boolean remove(QueryWrapper<PostAttachment> queryWrapper);

}
