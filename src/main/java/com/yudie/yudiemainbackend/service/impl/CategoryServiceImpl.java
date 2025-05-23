package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.model.entity.Category;
import com.yudie.yudiemainbackend.service.CategoryService;
import com.yudie.yudiemainbackend.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author lenovo
* @description 针对表【category(分类表)】的数据库操作Service实现
* @createDate 2025-05-23 17:09:48
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




