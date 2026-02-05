package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.mapper.CategoryMapper;
import com.yudie.yudiemainbackend.model.entity.Category;
import com.yudie.yudiemainbackend.model.vo.CategoryVO;
import com.yudie.yudiemainbackend.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【category(分类表)】的数据库操作Service实现
* @createDate 2025-05-23 17:09:48
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    /**
     * 获取指定类型的分类列表
     * @param type 分类类型：0-图片分类 1-帖子分类
     */
    @Override
    public List<String> listCategoryByType(Integer type) {
        return this.baseMapper.listCategoryByType(type);
    }

    /**
     * 获取分类包装类列表
     * @param records 分类
     * @return 分类包装类列表
     */
    @Override
    public List<CategoryVO> listCategoryVO(List<Category> records) {
        if(CollUtil.isEmpty(records)){
            return null;
        }
        return records.stream().map(this::getCategoryVO).collect(Collectors.toList());
    }

    /**
     * 获取分类包装类
     * @param category 分类
     * @return 分类包装类
     */
    @Override
    public CategoryVO getCategoryVO(Category category) {
        if(category == null){
            return null;
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    /**
     * 根据名称和类型查找分类
     * @param categoryName 分类名
     * @param type 类型
     * @return 分类
     */
    @Override
    public List<CategoryVO> findCategory(String categoryName, Integer type) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("categoryName", categoryName);
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        List<Category> categoryList = this.baseMapper.selectList(queryWrapper);
        return listCategoryVO(categoryList);
    }

    /**
     * 添加指定类型的分类
     * @param categoryName 分类名
     * @param type 类型
     * @return 是否成功
     */
    @Override
    public boolean addCategory(String categoryName, Integer type) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        return this.save(category);
    }
}




