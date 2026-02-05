package com.yudie.yudiemainbackend.mapper;

import com.yudie.yudiemainbackend.model.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2025-05-23 17:11:49
* @Entity com.yudie.yudiemainbackend.model.entity.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 查找标签
     * @return 标签
     */
    @Select("select tagName from tag where isDelete = 0")
    List<String> listTag();

}




