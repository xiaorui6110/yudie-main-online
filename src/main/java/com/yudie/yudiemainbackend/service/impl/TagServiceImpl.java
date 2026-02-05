package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.model.entity.Tag;
import com.yudie.yudiemainbackend.model.vo.TagVO;
import com.yudie.yudiemainbackend.service.TagService;
import com.yudie.yudiemainbackend.mapper.TagMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiaorui
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2025-05-23 17:11:49
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    /**
     * 获取标签列表
     * @return 标签列表
     */
    @Override
    public List<String> listTag() {
        return this.baseMapper.listTag();
    }

    /**
     * 获取标签包装类
     * @param tag 标签
     * @return 标签包装类
     */
    @Override
    public TagVO getTagVO(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagVO tagVO = new TagVO();
        BeanUtil.copyProperties(tag, tagVO);
        return tagVO;
    }

    /**
     * 分页获取标签包装类
     * @param records 标签
     * @return 标签包装类
     */
    @Override
    public List<TagVO> listTagVOByPage(List<Tag> records) {
        if (CollUtil.isEmpty(records)) {
            return null;
        }
        return records.stream().map(this::getTagVO).collect(Collectors.toList());
    }

    /**
     * 添加标签
     * @param tagName 标签名
     * @return 是否成功
     */
    @Override
    public Boolean addTag(String tagName) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        return save(tag);
    }

    /**
     * 删除标签
     * @param id 标签 id
     * @return 是否成功
     */
    @Override
    public Boolean deleteTag(Long id) {
        return removeById(id);
    }

    /**
     * 搜索标签
     * @param tagName 标签名
     * @return 标签包装类
     */
    @Override
    public List<TagVO> searchTag(String tagName) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("tagName", tagName);
        List<Tag> tagList = baseMapper.selectList(queryWrapper);
        return listTagVOByPage(tagList);
    }
}




