package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.annotation.AuthCheck;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.PageRequest;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.constant.UserConstant;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.entity.Tag;
import com.yudie.yudiemainbackend.model.vo.TagVO;
import com.yudie.yudiemainbackend.service.TagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 标签实现类接口
 * @author: xiaorui
 * @date: 2025-05-25 14:54
 **/
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 获取所有标签
     * @param pageRequest 分页请求
     * @return 标签列表
     */
    @PostMapping("list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<TagVO>> listTagVOByPage(@RequestBody PageRequest pageRequest){
        long current = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();
        Page<Tag> tagPage = tagService.page(new Page<>(current, pageSize));
        Page<TagVO> tagVOPage = new Page<>(current, pageSize,tagPage.getTotal());
        List<TagVO> tagVOList = tagService.listTagVOByPage(tagPage.getRecords());
        tagVOPage.setRecords(tagVOList);
        return ResultUtils.success(tagVOPage);
    }

    /**
     * 添加标签
     * @param tagName 标签名
     * @return 是否成功
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addTag(String tagName){
        ThrowUtils.throwIf(tagName == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(tagService.addTag(tagName));
    }

    /**
     * 删除标签
     * @param id 标签 id
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteTag(Long id){
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(tagService.deleteTag(id));
    }

    /**
     * 查找标签
     * @param tagName 标签名
     * @return 标签列表
     */
    @PostMapping("/search")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<TagVO>> searchTag(String tagName){
        ThrowUtils.throwIf(tagName == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(tagService.searchTag(tagName));
    }

}
