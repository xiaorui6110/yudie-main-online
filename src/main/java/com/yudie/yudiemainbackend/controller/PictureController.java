package com.yudie.yudiemainbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.annotation.AuthCheck;
import com.yudie.yudiemainbackend.api.aliyunai.AliYunAiApi;
import com.yudie.yudiemainbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yudie.yudiemainbackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yudie.yudiemainbackend.api.imagesearch.baidu.ImageSearchApiFacade;
import com.yudie.yudiemainbackend.api.imagesearch.baidu.model.ImageSearchResult;
import com.yudie.yudiemainbackend.api.imagesearch.so.SoImageSearchApiFacade;
import com.yudie.yudiemainbackend.api.imagesearch.so.model.SoImageSearchResult;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.DeleteRequest;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.constant.CrawlerConstant;
import com.yudie.yudiemainbackend.constant.UserConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.yudie.yudiemainbackend.manager.auth.model.SpaceUserPermissionConstant;
import com.yudie.yudiemainbackend.model.dto.picture.*;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.PictureTagCategory;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.service.CategoryService;
import com.yudie.yudiemainbackend.service.PictureService;
import com.yudie.yudiemainbackend.service.TagService;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 图片实现类接口
 * @author: xiaorui
 * @date: 2025-05-25 11:43
 **/
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private CrawlerManager crawlerManager;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private TagService tagService;

    @Resource
    private CategoryService categoryService;

    /**
     * 上传图片（可重新上传）
     * @param multipartFile 图片
     * @param pictureUploadRequest 图片上传请求
     * @param request 请求
     * @return 图片包装类
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片
     * @param pictureUploadRequest 图片上传请求
     * @param request 请求
     * @return 图片包装类
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     * @param deleteRequest 删除请求
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     * @param picture 图片
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updatePicture(@RequestBody Picture picture) {
        return ResultUtils.success(pictureService.updatePicture(picture));
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     * @param id 图片 id
     * @return 图片
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }


    /**
     * 批量操作图片（仅管理员可用）
     * @param pictureOperation 图片操作
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/batchOption")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchOperationPicture(@RequestBody PictureOperation pictureOperation,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(pictureOperation == null || pictureOperation.getIds() == null
                || pictureOperation.getIds().isEmpty(), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR);
        boolean result = pictureService.batchOperationPicture(pictureOperation);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取图片包装类
     * @param id 图片 id
     * @param request 请求
     * @return 图片包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        return ResultUtils.success(pictureService.getPictureVOById(id, request));
    }

    /**
     * 分页获取图片包装类（仅管理员可用）
     * @param pictureQueryRequest 图片查询请求
     * @return 图片包装类
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表包装类（带缓存）
     * @param pictureQueryRequest 图片查询请求
     * @param request 请求
     * @return 图片列表包装类
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        return ResultUtils.success(pictureService.listPictureVOByPageWithCache(pictureQueryRequest, request));
    }

    /**
     * 分页获取图片列表包装类
     * @param pictureQueryRequest 图片查询请求
     * @param request 请求
     * @return 图片列表包装类
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        User loginUser = userService.isLogin(request);
        if (loginUser != null) {
            String userRole = loginUser.getUserRole();
            ThrowUtils.throwIf(userRole.equals(CrawlerConstant.BAN_ROLE),
                    ErrorCode.NOT_AUTH_ERROR, "封禁用户禁止获取数据,请联系管理员");
        }

        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR, "每页最多显示 50 条");
        crawlerManager.detectNormalRequest(request);
        return ResultUtils.success(pictureService.listPictureVOByPage(pictureQueryRequest, request));
    }

    /**
     * 编辑图片（给用户使用）
     * @param pictureEditRequest 图片编辑请求
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/edit")
    @Transactional(rollbackFor = Exception.class)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 审核图片
     * @param pictureReviewRequest 审核图片请求
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取图片
     * @param pictureUploadByBatchRequest 批量抓取图片请求
     * @param request 请求
     * @return 图片数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 根据颜色搜索图片
     * @param searchPictureByColorRequest 根据颜色搜索图片请求
     * @param request 请求
     * @return 图片
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest 图片批量编辑请求
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     * @param createPictureOutPaintingTaskRequest 创建扩图任务请求
     * @param request 请求
     * @return 响应
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     * @param taskId 任务ID
     * @return 任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

    /**
     * 以图搜图
     * @param searchPictureByPictureRequest 以图搜图请求
     * @return 图片列表
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(picture.getThumbnailUrl());
        return ResultUtils.success(resultList);
    }

    /**
     * 获取Top100图片列表(带缓存)
     * @param id 榜单类型ID
     * @return Top100图片列表
     */
    @GetMapping("/top100/{id}")
    public BaseResponse<List<PictureVO>> getTop100Picture(@PathVariable Long id) {
        return ResultUtils.success(pictureService.getTop100PictureWithCache(id));
    }

    /**
     * 获取关注图片列表
     * @param pictureQueryRequest 图片查询请求
     * @param request 请求·
     * @return 关注图片列表
     */
    @PostMapping("/follow")
    public BaseResponse<Page<PictureVO>> getFollowPicture(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                          HttpServletRequest request) {
        return ResultUtils.success(pictureService.getFollowPicture(request, pictureQueryRequest));
    }

    /**
     * 上传帖子图片
     * @param multipartFile 文件输入源
     * @param pictureUploadRequest 图片上传请求
     * @param request 请求
     * @return 图片
     */
    @PostMapping("/upload/postimage")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPostImage(@RequestPart("file") MultipartFile multipartFile,
                                                   PictureUploadRequest pictureUploadRequest,
                                                   HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPostPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 获取图片标签分类
     * @return 图片标签分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = tagService.listTag();
        List<String> categoryList = categoryService.listCategory();
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

}
