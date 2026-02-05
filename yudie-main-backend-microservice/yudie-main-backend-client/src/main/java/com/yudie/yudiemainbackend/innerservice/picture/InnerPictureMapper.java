package com.yudie.yudiemainbackend.innerservice.picture;

import com.yudie.yudiemainbackend.model.entity.Picture;

public interface InnerPictureMapper {

    Picture selectById(Long id);
}
