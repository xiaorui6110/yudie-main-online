package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.model.dto.search.SearchRequest;
import com.yudie.yudiemainbackend.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 搜索实现类接口
 * @author: siri
 * @date: 2025-05-30 14:18
 **/
@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private SearchService searchService;

    @PostMapping("/all")
    public BaseResponse<Page<?>> searchAll(@RequestBody SearchRequest searchRequest) {
        return ResultUtils.success(searchService.doSearch(searchRequest));
    }

    /**
     * 获取热门搜索关键词
     * @param type 搜索类型 (picture/user/post/space)
     * @param size 返回数量，默认9个
     * @return 热门搜索关键词列表
     */
    @GetMapping("/hot")
    public BaseResponse<List<String>> getHotSearchKeywords(
            @RequestParam(required = true) String type,
            @RequestParam(required = false, defaultValue = "9") Integer size) {
        if (StringUtils.isBlank(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索类型不能为空");
        }
        final int MAX_SIZE = 100;
        if (size <= 0 || size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "size必须在1-100之间");
        }
        if (!type.matches("^(picture|user|post|space)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的搜索类型");
        }
        return ResultUtils.success(searchService.getHotSearchKeywords(type, size));
    }

}
