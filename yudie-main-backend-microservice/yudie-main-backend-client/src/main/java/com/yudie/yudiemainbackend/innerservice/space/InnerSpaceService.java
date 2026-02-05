package com.yudie.yudiemainbackend.innerservice.space;


import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.yudie.yudiemainbackend.model.entity.Space;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;

public interface InnerSpaceService {

    Space getById(Serializable id);

    LambdaUpdateChainWrapper<Space> lambdaUpdate();

    LambdaQueryChainWrapper<Space> lambdaQuery();

    SpaceVO getSpaceVOById(long id, HttpServletRequest request);
}
