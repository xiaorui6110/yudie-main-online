package com.yudie.yudiemainbackend.mapper;

import com.yudie.yudiemainbackend.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【picture(图片表)】的数据库操作Mapper
* @createDate 2025-05-23 17:07:52
* @Entity com.yudie.yudiemainbackend.model.entity.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 获取图片-年
     * @return List<Picture>
     */
    List<Picture> getTop100PictureByYear();

    /**
     * 获取图片-月
     * @return List<Picture>
     */
    List<Picture> getTop100PictureByMonth();

    /**
     * 获取图片-周
     * @return List<Picture>
     */
    List<Picture> getTop100PictureByWeek();
}




