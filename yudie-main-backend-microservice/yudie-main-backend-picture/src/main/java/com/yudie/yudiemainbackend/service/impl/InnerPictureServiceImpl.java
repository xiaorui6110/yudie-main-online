package com.yudie.yudiemainbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.innerservice.picture.InnerPictureService;
import com.yudie.yudiemainbackend.model.dto.picture.PictureQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.service.PictureService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class InnerPictureServiceImpl implements InnerPictureService {

    @Resource
    private PictureService pictureService;

    @Override
    public Picture getById(long pictureId) {
        return pictureService.getById(pictureId);
    }

    @Override
    public UpdateChainWrapper<Picture> update() {
        return pictureService.update();
    }

    @Override
    public List<Picture> list(QueryWrapper<Picture> queryWrapper) {
        return pictureService.list(queryWrapper);
    }

    @Override
    public boolean remove(QueryWrapper<Picture> queryWrapper) {
        return pictureService.remove(queryWrapper);
    }

    @Override
    public PictureVO getPictureVOById(long id, HttpServletRequest request) {
        return pictureService.getPictureVOById(id, request);
    }

    @Override
    public List<PictureVO> getTop100Picture(Long id) {
        return pictureService.getTop100Picture(id);
    }

    @Override
    public boolean updateBatchById(List<Picture> pictures) {
        return pictureService.updateBatchById(pictures);
    }

    @Override
    public Page<Picture> page(Page<Picture> page, LambdaQueryWrapper<Picture> queryWrapper) {
        return pictureService.page(page, queryWrapper);
    }

    @Override
    public BaseMapper<Picture> getBaseMapper() {
        return pictureService.getBaseMapper();
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        return pictureService.getPictureVOPage(picturePage, request);
    }

    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return pictureService.getQueryWrapper(pictureQueryRequest).lambda();
    }
}
