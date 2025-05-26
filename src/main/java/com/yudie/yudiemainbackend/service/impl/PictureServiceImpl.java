package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.annotation.AuthCheck;
import com.yudie.yudiemainbackend.api.aliyunai.AliYunAiApi;
import com.yudie.yudiemainbackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.yudie.yudiemainbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yudie.yudiemainbackend.constant.CrawlerConstant;
import com.yudie.yudiemainbackend.constant.RedisConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CosManager;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.manager.upload.FilePictureUpload;
import com.yudie.yudiemainbackend.manager.upload.PictureUploadTemplate;
import com.yudie.yudiemainbackend.manager.upload.UrlPictureUpload;
import com.yudie.yudiemainbackend.model.dto.file.UploadPictureResult;
import com.yudie.yudiemainbackend.model.dto.picture.*;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.enums.OperationEnum;
import com.yudie.yudiemainbackend.model.enums.PictureReviewStatusEnum;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.PictureService;
import com.yudie.yudiemainbackend.mapper.PictureMapper;
import com.yudie.yudiemainbackend.service.UserService;
import com.yudie.yudiemainbackend.utils.ColorSimilarUtils;
import com.yudie.yudiemainbackend.utils.ColorTransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author lenovo
* @description 针对表【picture(图片表)】的数据库操作Service实现
* @createDate 2025-05-23 17:07:52
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CrawlerManager crawlerManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

    /**
     * 校验图片
     * @param picture 图片
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 上传图片
     * @param  inputSource 文件输入源
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 用户
     * @return 图片
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(inputSource), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(pictureUploadRequest), ErrorCode.PARAMS_ERROR);

        // TODO 2. 校验空间 spaceService

        Long spaceId = pictureUploadRequest
                .getSpaceId();

        // 3. 判断是新增还是更新图片
        Long pictureId = pictureUploadRequest.getId();
        // 如果是更新图片，先查询图片信息
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR);
            // 校验空间
            // 没传 spaceId，则复用原有图片的 spaceId（这样也兼容了公共图库）
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原图片的空间 id 一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
        // 4. 上传图片
        String uploadPathPrefix;
        if (spaceId == null) {
            // 公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            // 其他空间
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        // 根据 inputSource 的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 5. 构建图片入库信息
        Picture picture = new Picture();
        // 指定空间 id
        picture.setSpaceId(spaceId);
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        // 支持外层传递图片名称
        String picName = uploadPictureResult.getPicName();
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        // 转换为标准颜色
        picture.setPicColor(ColorTransformUtils.getStandardColor(uploadPictureResult.getPicColor()));
        picture.setCategory(pictureUploadRequest.getCategoryName());
        picture.setTags(pictureUploadRequest.getTagName());
        picture.setUserId(loginUser.getId());
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 6. 入库
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");

            // TODO 更新空间的使用额度 ，待空间模块完成

            return picture;
        });
        // 7. 返回图片包装类
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取图片包装类（单条）
     * @param picture 图片
     * @param request 请求
     * @return 图片包装类
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        if (picture == null) {
            return null;
        }
        // 增加浏览量
        incrementViewCount(picture.getId(), request);
        incrementViewCount(picture.getId(), request);
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 设置实时浏览量
        pictureVO.setViewCount(getViewCount(picture.getId()));
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        // TODO 设置点赞状态 - 使用新的通用点赞表 likeRecordService shareRecordService

        return pictureVO;
    }


    /**
     * 增加图片浏览量
     * @param pictureId 图片 id
     * @param request 请求
     */
    private void incrementViewCount(Long pictureId, HttpServletRequest request) {
        // 检查是否需要增加浏览量
        if (!crawlerManager.detectViewRequest(request, pictureId)) {
            return;
        }
        // 使用 Redis 进行计数
        String viewCountKey = String.format("picture:viewCount:%d", pictureId);
        String lockKey = String.format("picture:viewCount:lock:%d", pictureId);

        try {
            // 获取分布式锁
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                // 增加浏览量
                stringRedisTemplate.opsForValue().increment(viewCountKey);

                // 当浏览量达到一定阈值时，更新数据库
                String viewCountStr = stringRedisTemplate.opsForValue().get(viewCountKey);
                if (viewCountStr != null && Long.parseLong(viewCountStr) % 100 == 0) {
                    this.update()
                            .setSql("viewCount = viewCount + " + viewCountStr)
                            .eq("id", pictureId)
                            .update();
                    // 更新后重置 Redis 计数
                    stringRedisTemplate.delete(viewCountKey);
                }
            }
        } finally {
            // 释放锁
            stringRedisTemplate.delete(lockKey);
        }
    }


    /**
     * 获取图片 VO（内部使用）
     * @param picture 图片
     * @param loginUser 用户
     * @return 图片包装类
     */
    private PictureVO getPictureVOInternal(Picture picture, User loginUser) {
        if (picture == null) {
            return null;
        }
        // 增加浏览量
        incrementViewCount(picture.getId(), null);
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 设置实时浏览量
        pictureVO.setViewCount(getViewCount(picture.getId()));
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }

        // TODO 设置点赞状态 - 使用新的通用点赞表 likeRecordService shareRecordService

        return pictureVO;
    }

    /**
     * 获取图片包装类（分页）
     * @param picturePage 图片分页
     * @param request 请求
     * @return 图片分页
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 获取登录用户，不登录也能获取到图片信息
        User loginUser = userService.isLogin(request);
        // 填充信息
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(picture -> getPictureVOInternal(picture, loginUser))
                .collect(Collectors.toList());
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 获取查询条件
     * @param pictureQueryRequest 图片查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            return null;
        }
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(
                    qw -> qw.like("name", searchText)
                            .or()
                            .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // >= startEditTime
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < endEditTime
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            /* and (tag like "%\"Java\"%" and like "%\"Python\"%") */
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 审核图片（管理员）
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR);
        // 3. 校验审核状态是否重复
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 4. 更新图片审核状态
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片审核失败，数据库操作失败");

        // TODO 5. 同步更新 ES 数据

    }

    /**
     * 填充审核参数
     * @param picture 图片
     * @param loginUser 用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        // 管理员自动过审
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动审核");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，编辑和上传都需要审核，默认状态是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量上传图片
     * @param pictureUploadByBatchRequest 批量上传图片请求
     * @param loginUser 用户
     * @return 图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 1. 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        // 名称前缀默认等于搜索关键词
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 2. 抓取网页内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 3. 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = Objects.requireNonNull(div).select("img.mimg");
        // 4. 上传图片
        // 遍历元素，依次处理上传图片
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            // 处理图片的地址，防止转义或者和对象存储冲突的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            pictureUploadRequest.setCategoryName(pictureUploadByBatchRequest.getCategoryName());
            pictureUploadRequest.setTagName(JSONUtil.toJsonStr(pictureUploadByBatchRequest.getTagName()));
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功，id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 批量操作图片（删除/审核）
     * @param pictureOperation 图片操作
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchOperationPicture(PictureOperation pictureOperation) {
        // 获取批量操作的类型
        long operationType = pictureOperation.getOperationType();
        // 获取批量操作图片的 id
        List<Long> pictureIds = pictureOperation.getIds();
        boolean result = false;
        // 批量删除
        if (operationType == OperationEnum.DELETE.getValue()) {
            // 删除图片
            List<Picture> pictureList = this.listByIds(pictureIds);
            ThrowUtils.throwIf(ObjUtil.isEmpty(pictureList), ErrorCode.NOT_FOUND_ERROR);
            result = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    // 删除 MySQL 数据
                    boolean deleteResult = this.removeByIds(pictureIds);
                    if (!deleteResult) {
                        return false;
                    }

                    // TODO 删除 ES 数据

                    // 删除图片文件
                    for (Picture oldPicture : pictureList) {
                        this.clearPictureFile(oldPicture);
                    }
                    return true;
                } catch (Exception e) {
                    log.error("批量删除图片失败", e);
                    return false;
                }
            }));
        }
        // 批量审核
        else if (operationType == OperationEnum.APPROVE.getValue() || operationType == OperationEnum.REJECT.getValue()) {
            try {
                // 设置审核状态
                Integer reviewStatus = operationType == OperationEnum.APPROVE.getValue() ?
                        PictureReviewStatusEnum.PASS.getValue() : PictureReviewStatusEnum.REJECT.getValue();
                // 更新 MySQL 数据
                result =update()
                        .set("reviewStatus", reviewStatus)
                        .set("reviewTime", new Date())
                        .set("reviewMessage", operationType == OperationEnum.APPROVE.getValue() ?
                                "批量审核通过" : "批量审核不通过")
                        .in("id", pictureIds)
                        .update();

                // TODO 批量更新 ES 数据

            } catch (Exception e) {
                log.error("批量审核图片失败", e);
                return false;
            }
        }
        return result;
    }


    /**
     * 清理图片文件
     * @param oldPicture 旧图片
     */
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        if (oldPicture == null) {
            // 若 oldPicture 为 null，直接返回，避免空指针异常
            return;
        }
        // 判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        // 删除图片
        cosManager.deleteObject(pictureUrl);
        // 删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    /**
     * 检查图片权限
     * @param loginUser 用户
     * @param picture 图片
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
            }
        }
    }

    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 用户
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验图片是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR);
        // 开启事务
        Long finalSpaceId = oldPicture.getSpaceId();
        transactionTemplate.execute(status -> {
            try {
                // 操作数据库
                boolean result = this.removeById(pictureId);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

                // TODO 更新空间的使用额度，释放额度

                // TODO 从 ES 删除

                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    /**
     * 根据颜色搜索图片
     * @param spaceId 空间ID
     * @param picColor 颜色
     * @param loginUser 用户
     * @return 图片列表
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2. TODO 校验空间权限 spaceService

        // 3. 查询该空间下的所有图片（必须要有主色调）
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        // 如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }
        // 将颜色字符串转换为主色调
        Color targetColor = Color.decode(picColor);
        // 4. 计算相似度并排序
        List<Picture> sortedPictureList = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    // 没有主色调的图片会默认排序到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 计算相似度
                    // 越大越相似
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                // 取前 12 个
                .limit(12)
                .collect(Collectors.toList());
        // 5. 返回结果
        return sortedPictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }

    /**
     * 编辑图片
     * @param pictureEditRequest 图片编辑请求
     * @param loginUser 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意 Tags 将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setName(pictureEditRequest.getName());
        picture.setCategory(pictureEditRequest.getCategory());
        picture.setIntroduction(pictureEditRequest.getIntroduction());
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 保留原有数据中的一些字段
        picture.setUrl(oldPicture.getUrl());
        picture.setThumbnailUrl(oldPicture.getThumbnailUrl());
        picture.setPicSize(oldPicture.getPicSize());
        picture.setPicWidth(oldPicture.getPicWidth());
        picture.setPicHeight(oldPicture.getPicHeight());
        picture.setPicScale(oldPicture.getPicScale());
        picture.setPicFormat(oldPicture.getPicFormat());
        picture.setPicColor(oldPicture.getPicColor());
        picture.setUserId(oldPicture.getUserId());
        picture.setSpaceId(oldPicture.getSpaceId());
        picture.setCreateTime(oldPicture.getCreateTime());
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // TODO 同步更新 ES 数据

    }

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest 图片批量编辑请求
     * @param loginUser 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        // 1. 获取和校验参数
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2. TODO 校验空间权限 spaceService

        // 3. 查询指定图片（仅选择需要的字段）
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }

        // 4. 更新分类和标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });

        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);

        // 5. 操作数据库进行批量更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量编辑失败");

        // 6. TODO 同步更新 ES 数据

    }


    /**
     * 填充图片名称
     * nameRule 格式：图片{序号}
     * @param pictureList 图片列表
     * @param nameRule 格式
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (StrUtil.isBlank(nameRule) || CollUtil.isEmpty(pictureList)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }


    /**
     * 爬虫检测
     * @param request 请求
     */
    @Override
    public void crawlerDetect(HttpServletRequest request) {
        crawlerManager.detectNormalRequest(request);
    }


    /**
     * 获取热门图片
     * @param id 图片ID
     * @return 图片列表
     */
    @Override
    public List<PictureVO> getTop100Picture(Long id) {
        List<Picture> pictureList = this.getTop100PictureList(id);
        // 批量获取浏览量
        Map<Long, Long> viewCountMap = new HashMap<>();
        List<String> viewCountKeys = pictureList.stream()
                .map(picture -> String.format("picture:viewCount:%d", picture.getId()))
                .collect(Collectors.toList());
        if (!viewCountKeys.isEmpty()) {
            List<String> redisViewCounts = stringRedisTemplate.opsForValue().multiGet(viewCountKeys);
            for (int i = 0; i < pictureList.size(); i++) {
                Picture picture = pictureList.get(i);
                String redisCount = Objects.requireNonNull(redisViewCounts).get(i);
                long baseCount = picture.getViewCount() != null ? picture.getViewCount() : 0L;
                long increment = redisCount != null ? Long.parseLong(redisCount) : 0L;
                viewCountMap.put(picture.getId(), baseCount + increment);
            }
        }
        // 批量获取用户信息
        Set<Long> userIds = pictureList.stream()
                .map(Picture::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        return pictureList.stream()
                .map(picture -> {
                    PictureVO pictureVO = PictureVO.objToVo(picture);
                    // 设置实时浏览量
                    pictureVO.setViewCount(viewCountMap.getOrDefault(picture.getId(), 0L));
                    // 设置用户信息
                    Long userId = picture.getUserId();
                    if (userId != null && userId > 0) {
                        User user = userMap.get(userId);
                        if (user != null) {
                            UserVO userVO = userService.getUserVO(user);
                            pictureVO.setUser(userVO);
                        }
                    }
                    // 默认未点赞
                    pictureVO.setIsLiked(0);
                    return pictureVO;
                })
                .collect(Collectors.toList());
    }


    /**
     * 获取热门图片列表
     * @param id 图片ID
     * @return 图片列表
     */
    private List<Picture> getTop100PictureList(Long id) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0)
                .isNull("spaceId")
                .eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue());
        // 根据不同时间范围查询
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        switch (id.intValue()) {
            case 1:
                // 日榜
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case 2:
                // 周榜
                calendar.add(Calendar.WEEK_OF_MONTH, -1);
                break;
            case 3:
                // 月榜
                calendar.add(Calendar.MONTH, -1);
                break;
            case 4:
                // 总榜
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final int idInt = 4;
        if (id < idInt) {
            queryWrapper.ge("createTime", calendar.getTime());
        }
        // 修改排序规则，加入浏览量因素
        queryWrapper.orderByDesc(
                "(likeCount * 0.4 + commentCount * 0.3 + viewCount * 0.2 + shareCount * 0.1)"
        );
        // 拼接 sql（会有 sql 注入风险）
        queryWrapper.last("LIMIT 100");
        return this.list(queryWrapper);
    }


    /**
     * 获取关注图片
     * @param request 请求
     * @param pictureQueryRequest 图片查询请求
     * @return 图片列表
     */
    @Override
    public Page<PictureVO> getFollowPicture(HttpServletRequest request, PictureQueryRequest pictureQueryRequest) {

        // TODO 用户关注模块 userfollowsService


        return null;
    }


    /**
     * 上传帖子图片
     * @param inputSource 文件输入源
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 用户
     * @return 图片信息
     */
    @Override
    public PictureVO uploadPostPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 设置帖子图片的特殊属性
        if (pictureUploadRequest == null) {
            pictureUploadRequest = new PictureUploadRequest();
        }
        // 使用 -1 表示帖子图片
        pictureUploadRequest.setSpaceId(-1L);
        // 上传图片，得到图片信息
        String uploadPathPrefix = String.format("post/%s", loginUser.getId());
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        // 指定为帖子图片
        picture.setSpaceId(-1L);
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        // 支持外层传递图片名称
        String picName = uploadPictureResult.getPicName();
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        // 转换为标准颜色
        picture.setPicColor(ColorTransformUtils.getStandardColor(uploadPictureResult.getPicColor()));
        picture.setCategory(pictureUploadRequest.getCategoryName());
        picture.setTags(pictureUploadRequest.getTagName());
        picture.setUserId(loginUser.getId());
        // 帖子图片特殊处理：自动设置为审核通过状态
        picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        picture.setReviewMessage("帖子照片自动过审");
        // 操作数据库
        transactionTemplate.execute(status -> {
            // 插入数据
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            return picture;
        });

        return PictureVO.objToVo(picture);
    }


    /**
     *  获取图片浏览量
     * @param pictureId 图片ID
     * @return 浏览量
     */
    @Override
    public long getViewCount(Long pictureId) {
        // 1. 校验参数
        ThrowUtils.throwIf(pictureId == null, ErrorCode.PARAMS_ERROR);
        // 2. 先从 Redis 中获取增量
        String viewCountKey = String.format("picture:viewCount:%d", pictureId);
        String incrementCount = stringRedisTemplate.opsForValue().get(viewCountKey);
        // 3. 再从 MySQL 中获取基础浏览量
        Picture picture = this.getById(pictureId);
        if (picture == null) {
            return 0L;
        }
        // 4. 合并
        long baseCount = picture.getViewCount() != null ? picture.getViewCount() : 0L;
        long increment = incrementCount != null ? Long.parseLong(incrementCount) : 0L;
        return baseCount + increment;
    }

    /**
     * 更新图片
     * @param picture 图片信息
     * @return 是否成功
     */
    @Override
    public boolean updatePicture(Picture picture) {
        // 更新数据库
        boolean success = this.updateById(picture);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        // TODO 同步更新 ES 数据

        return true;
    }


    /**
     * 获取图片VO
     * @param id 图片ID
     * @param request HTTP请求
     * @return 图片VO
     */
    @Override
    public PictureVO getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        // TODO 空间权限校验 spaceService 获取权限列表 spaceUserAuthManager

        // 获取图片VO
        PictureVO pictureVO = this.getPictureVO(picture, request);

        return pictureVO;
    }


    /**
     * 分页获取图片VO（带缓存）
     * @param pictureQueryRequest 查询请求
     * @param request HTTP请求
     * @return 图片VO列表
     */
    @Override
    public Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 用户权限校验
        User currentUser = userService.getLoginUser(request);
        if (currentUser != null) {
            String userRole = currentUser.getUserRole();
            ThrowUtils.throwIf(userRole.equals(CrawlerConstant.BAN_ROLE),
                    ErrorCode.NOT_AUTH_ERROR, "封禁用户禁止获取数据,请联系管理员");
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        crawlerDetect(request);
        // 处理查询条件
        if (spaceId == null) {
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        }
        // 构建缓存 key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = RedisConstant.PUBLIC_PIC_REDIS_KEY_PREFIX + hashKey;
        // 查询数据库
        Page<Picture> picturePage = this.page(new Page<>(current, size),
                this.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = this.getPictureVOPage(picturePage, request);
        // 更新缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        return pictureVOPage;
    }


    /**
     * 获取热门图片（带缓存）
     * @param id 榜单类型ID
     * @return 图片列表
     */
    @Override
    public List<PictureVO> getTop100PictureWithCache(Long id) {
        // 构建缓存key
        String cacheKey = RedisConstant.TOP_100_PIC_REDIS_KEY_PREFIX + id;
        // 尝试从缓存获取
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return JSONUtil.toList(cachedValue, PictureVO.class);
        }
        // 缓存未命中,查询数据库
        List<Picture> pictureList = this.getTop100PictureList(id);
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(picture -> this.getPictureVO(picture, null))
                .collect(Collectors.toList());
        // 更新缓存
        int cacheExpireTime = (int) (RedisConstant.TOP_100_PIC_REDIS_KEY_EXPIRE_TIME + RandomUtil.randomInt(0, 6000));
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(pictureVOList),
                cacheExpireTime, TimeUnit.SECONDS);
        return pictureVOList;
    }

    /**
     * 分页获取图片VO
     * @param pictureQueryRequest 查询请求
     * @param request HTTP请求
     * @return 图片VO列表
     */
    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        Long spaceId = pictureQueryRequest.getSpaceId();
        Long userId = pictureQueryRequest.getUserId();
        User loginUser = null;

        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            // 未登录用户只能看公开图库
            if (spaceId != null || userId != null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
        }
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);
        // 处理不同的查询场景
        if (spaceId != null) {
            // 空间内图片查询
            handleSpaceQuery(spaceId, loginUser, queryWrapper);
        } else if (userId != null) {
            // 用户图片查询
            handleUserQuery(userId, loginUser, queryWrapper);
        } else {
            // 公开图库查询
            handlePublicQuery(queryWrapper);
        }
        // 执行查询
        Page<Picture> picturePage = this.page(
                new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                queryWrapper
        );
        // 返回结果
        return getPictureVOPage(picturePage, request);
    }


    /**
     * 处理空间图片查询
     * @param spaceId 空间ID
     * @param loginUser 用户
     * @param queryWrapper 查询条件
     */
    private void handleSpaceQuery(Long spaceId, User loginUser, QueryWrapper<Picture> queryWrapper) {

        // TODO 检查空间是否存在 spaceService

        // TODO 检查是否是空间所有者

        // TODO 检查查看权限

        queryWrapper.eq("spaceId", spaceId);
    }


    /**
     * 处理用户图片查询
     * @param userId 用户ID
     * @param loginUser 用户
     * @param queryWrapper 查询条件
     */
    private void handleUserQuery(Long userId, User loginUser, QueryWrapper<Picture> queryWrapper) {
        // 只能查看自己的图片
        ThrowUtils.throwIf(loginUser == null || !loginUser.getId().equals(userId),
                ErrorCode.NOT_AUTH_ERROR, "只能查看自己的图片");
        queryWrapper.eq("userId", userId)
                .isNull("spaceId");
    }


    /**
     * 处理公开图库查询
     * @param queryWrapper 处理公开图库查询
     */
    private void handlePublicQuery(QueryWrapper<Picture> queryWrapper) {
        // 获取当前用户
        User loginUser = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            loginUser = userService.isLogin(request);
            // 封禁用户不能查看公共图库
            if (loginUser != null && CrawlerConstant.BAN_ROLE.equals(loginUser.getUserRole())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "封禁用户禁止查看图片,请联系管理员");
            }
        } catch (Exception ignored) {
            // 未登录用户可以继续查看
        }
        // 公开图库只显示审核通过的图片
        queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue())
                .isNull("spaceId");
    }


    /**
     * 创建扩图任务
     * @param createPictureOutPaintingTaskRequest 创建扩图任务请求
     * @param loginUser 用户
     * @return 扩图任务
     */
    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(
            CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
        // 创建扩图任务
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);

    }
}
