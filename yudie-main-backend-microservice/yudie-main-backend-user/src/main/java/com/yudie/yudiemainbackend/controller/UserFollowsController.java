package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.innerservice.picture.InnerPictureService;
import com.yudie.yudiemainbackend.model.dto.picture.PictureQueryRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserFollowsAddRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserFollowsIsFollowsRequest;
import com.yudie.yudiemainbackend.model.dto.userfollows.UserfollowsQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.enums.PictureReviewStatusEnum;
import com.yudie.yudiemainbackend.model.vo.FollowersAndFansVO;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.UserfollowsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description: 用户关注实现类接口
 * @author: xiaorui
 * @date: 2025-05-28 22:17
 **/
@Slf4j
@RestController
@RequestMapping("/userfollows")
public class UserFollowsController {

    @Resource
    private UserfollowsService userfollowsService;

    @Resource
    private InnerPictureService pictureService;

    /**
     * 关注、取关
     * @param userFollowsAddRequest 用户关注请求
     * @return 是否成功
     */
    @PostMapping("/adduserfollows")
    public BaseResponse<Boolean> addUserFollows(@RequestBody UserFollowsAddRequest userFollowsAddRequest){
        return ResultUtils.success(userfollowsService.addUserFollows(userFollowsAddRequest));
    }

    /**
     * 判断是否关注
     * @param userFollowsIsFollowsRequest 用户判断是否关注请求
     * @return 是否成功
     */
    @PostMapping("/findisfollow")
    public BaseResponse<Boolean> findIsFollow(@RequestBody UserFollowsIsFollowsRequest userFollowsIsFollowsRequest){
        return ResultUtils.success(userfollowsService.findIsFollow(userFollowsIsFollowsRequest));
    }

    /**
     * 获取关注或粉丝列表
     * @param userfollowsQueryRequest 用户获取关注或粉丝列表请求
     * @return 关注或粉丝列表
     */
    @PostMapping("/getfolloworfanlist")
    public BaseResponse<Page<UserVO>> getFollowOrFanList(@RequestBody UserfollowsQueryRequest userfollowsQueryRequest){
        return ResultUtils.success(userfollowsService.getFollowOrFanList(userfollowsQueryRequest));
    }

    /**
     * 获取关注和粉丝的数量
     * @param id 用户 id
     * @return 关注和粉丝的数量
     */
    @PostMapping("/getfollowandfanscount/{id}")
    public BaseResponse<FollowersAndFansVO> getFollowAndFansCount(@PathVariable Long id){
        return ResultUtils.success(userfollowsService.getFollowAndFansCount(id));

    }

    /**
     * 得到关注或者粉丝的公共的图片数据
     * @param pictureQueryRequest 图片查询请求
     * @param request 请求
     * @return 图片数据
     */
    @PostMapping("/getfolloworfanpicture")
    public BaseResponse<Page<PictureVO>> getFollowOrFanPicture(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                               HttpServletRequest request){
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureQueryRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "用户 id 不能为空");
        pictureQueryRequest.setUserId(pictureQueryRequest.getUserId());
        pictureQueryRequest.setNullSpaceId(true);
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

}
