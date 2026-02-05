package com.yudie.yudiemainbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yudie.yudiemainbackend.model.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【category(分类表)】的数据库操作Mapper
* @createDate 2025-05-23 17:09:48
* @Entity com.yudie.yudiemainbackend.model.entity.Category
*/
public interface CategoryMapper extends BaseMapper<Category> {
    /**
     * 查找分类名
     * @param type 类型
     * @return 分类名
     */
    @Select("select categoryName from category where isDelete = 0 and type = #{type}")
    List<String> listCategoryByType(@Param("type") Integer type);
}




