package com.yudie.yudiemainbackend.innerservice.picture;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.model.dto.picture.PictureQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface InnerPictureService {

    Picture getById(long pictureId);

    UpdateChainWrapper<Picture> update();

    List<Picture> list(QueryWrapper<Picture> queryWrapper);

    boolean remove(QueryWrapper<Picture> queryWrapper);

    PictureVO getPictureVOById(long id, HttpServletRequest request);

    List<PictureVO> getTop100Picture(Long id);

    boolean updateBatchById(List<Picture> pictures);

    Page<Picture> page(Page<Picture> page, LambdaQueryWrapper<Picture> queryWrapper);

    BaseMapper<Picture> getBaseMapper();

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);
}
