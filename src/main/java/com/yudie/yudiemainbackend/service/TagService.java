package com.yudie.yudiemainbackend.service;

import com.yudie.yudiemainbackend.model.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.vo.TagVO;

import java.util.List;

/**
* @author lenovo
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2025-05-23 17:11:49
*/
public interface TagService extends IService<Tag> {

    /**
     * 获取标签列表
     * @return 标签列表
     */
    List<String> listTag();

    /**
     * 获取标签包装类
     * @param tag 标签
     * @return 标签包装类
     */
    TagVO getTagVO(Tag tag);

    /**
     * 分页获取标签包装类
     * @param records 标签
     * @return 标签包装类
     */
    List<TagVO> listTagVOByPage(List<Tag> records);

    /**
     * 添加标签
     * @param tagName 标签名
     * @return 是否成功
     */
    Boolean addTag(String tagName);

    /**
     * 删除标签
     * @param id 标签 id
     * @return 是否成功
     */
    Boolean deleteTag(Long id);

    /**
     * 搜索标签
     * @param tagName 标签名
     * @return 标签包装类
     */
    List<TagVO> searchTag(String tagName);




}
