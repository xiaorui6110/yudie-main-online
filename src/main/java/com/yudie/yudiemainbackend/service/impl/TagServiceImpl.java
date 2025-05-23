package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.model.entity.Tag;
import com.yudie.yudiemainbackend.service.TagService;
import com.yudie.yudiemainbackend.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author lenovo
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2025-05-23 17:11:49
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




