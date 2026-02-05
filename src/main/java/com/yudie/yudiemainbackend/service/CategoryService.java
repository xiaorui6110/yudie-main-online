package com.yudie.yudiemainbackend.service;

import com.yudie.yudiemainbackend.model.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.vo.CategoryVO;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【category(分类表)】的数据库操作Service
* @createDate 2025-05-23 17:09:48
*/
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类列表，不传类型默认为图片分类
     * @return 分类列表
     */
    default List<String> listCategory() {
        // 默认图片分类
        return listCategoryByType(0);
    }

    /**
     * 获取指定类型的分类列表
     * @param type 分类类型：0-图片分类 1-帖子分类
     */
    List<String> listCategoryByType(Integer type);

    /**
     * 获取分类包装类列表
     * @param records 分类
     * @return 分类包装类列表
     */
    List<CategoryVO> listCategoryVO(List<Category> records);

    /**
     * 获取分类包装类
     * @param category 分类
     * @return 分类包装类
     */
    CategoryVO getCategoryVO(Category category);

    /**
     * 根据名称查找分类，不传类型默认为图片分类
     * @param categoryName 分类名
     * @return 分类包装类
     */
    default List<CategoryVO> findCategory(String categoryName) {
        // 默认图片分类
        return findCategory(categoryName, 0);
    }

    /**
     * 根据名称和类型查找分类
     * @param categoryName 分类名
     * @param type 类型
     * @return 分类
     */
    List<CategoryVO> findCategory(String categoryName, Integer type);

    /**
     * 添加分类，不传类型默认为图片分类
     * @param categoryName 分类名
     * @return 是否成功
     */
    default boolean addCategory(String categoryName) {
        // 默认图片分类
        return addCategory(categoryName, 0);
    }

    /**
     * 添加指定类型的分类
     * @param categoryName 分类名
     * @param type 类型
     * @return 是否成功
     */
    boolean addCategory(String categoryName, Integer type);
}
