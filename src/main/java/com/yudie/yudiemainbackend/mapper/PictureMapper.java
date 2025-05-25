package com.yudie.yudiemainbackend.mapper;

import com.yudie.yudiemainbackend.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author lenovo
* @description 针对表【picture(图片表)】的数据库操作Mapper
* @createDate 2025-05-23 17:07:52
* @Entity com.yudie.yudiemainbackend.model.entity.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    List<Picture> getTop100PictureByYear();

    List<Picture> getTop100PictureByMonth();

    List<Picture> getTop100PictureByWeek();
}




