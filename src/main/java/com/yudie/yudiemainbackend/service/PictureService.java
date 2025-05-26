package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yudie.yudiemainbackend.model.dto.picture.*;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lenovo
* @description 针对表【picture(图片表)】的数据库操作Service
* @createDate 2025-05-23 17:07:52
*/
public interface PictureService extends IService<Picture> {


    /**
     * 校验图片
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     * @param inputsource 文件输入源
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 用户
     * @return 图片
     */
    PictureVO uploadPicture(Object inputsource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取图片包装类（单条）
     * @param picture 图片
     * @param request 请求
     * @return 图片包装类
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     * @param picturePage 图片分页
     * @param request 请求
     * @return 图片分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询条件
     * @param pictureQueryRequest 图片查询请求
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 审核图片
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser 用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     * @param picture 图片
     * @param loginUser 用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量上传图片
     * @param pictureUploadByBatchRequest 批量上传图片请求
     * @param loginUser 用户
     * @return 图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 批量操作图片
     * @param pictureOperation 图片操作
     * @return 是否成功
     */
    boolean batchOperationPicture(PictureOperation pictureOperation);

    /**
     * 清理图片文件
     * @param oldPicture 旧图片
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 校验空间图片的权限
     * @param loginUser 用户
     * @param picture 图片
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 用户
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 根据颜色搜索图片
     * @param spaceId 空间ID
     * @param picColor 颜色
     * @param loginUser 用户
     * @return 图片
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 编辑图片
     * @param pictureEditRequest 图片编辑请求
     * @param loginUser 用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest 图片批量编辑请求
     * @param loginUser 用户
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);


    /**
     * 创建扩图任务
     *
     * @param createPictureOutPaintingTaskRequest 创建扩图任务请求
     * @param loginUser 用户
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(
            CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);


    /**
     * 爬虫检测
     * @param request 请求
     */
    void crawlerDetect(HttpServletRequest request);

    /**
     * 获取Top100图片列表
     * @param id 图片ID
     * @return Top100图片列表
     */
    List<PictureVO> getTop100Picture(Long id);

    /**
     * 获取关注图片列表
     * @param request 请求
     * @param pictureQueryRequest 图片查询请求
     * @return 关注图片列表
     */
    Page<PictureVO> getFollowPicture(HttpServletRequest request, PictureQueryRequest pictureQueryRequest);

    /**
     * 上传帖子图片
     * @param inputSource 文件输入源
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 用户
     * @return 图片
     */
    PictureVO uploadPostPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取图片浏览量
     * @param pictureId 图片ID
     * @return 浏览量
     */
    long getViewCount(Long pictureId);

    /**
     * 更新图片信息
     * @param picture 图片信息
     * @return 更新结果
     */
    boolean updatePicture(Picture picture);

    /**
     * 获取图片详情(带权限校验)
     * @param id 图片ID
     * @param request HTTP请求
     * @return 图片详情VO
     */
    PictureVO getPictureVOById(long id, HttpServletRequest request);

    /**
     * 分页获取图片列表(带缓存)
     * @param pictureQueryRequest 查询请求
     * @param request HTTP请求
     * @return 分页图片列表
     */
    Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 获取Top100图片列表(带缓存)
     * @param id 榜单类型ID
     * @return Top100图片列表
     */
    List<PictureVO> getTop100PictureWithCache(Long id);

    /**
     * 分页获取图片列表（封装类）
     * @param pictureQueryRequest 查询请求
     * @param request HTTP请求
     * @return 分页图片列表
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);


}
