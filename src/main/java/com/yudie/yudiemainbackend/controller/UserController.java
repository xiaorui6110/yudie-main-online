package com.yudie.yudiemainbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.annotation.AuthCheck;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.DeleteRequest;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.constant.CommonValue;
import com.yudie.yudiemainbackend.constant.UserConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.model.dto.user.*;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.vo.LoginUserVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 用户实现类接口
 * @author: xiaorui
 * @date: 2025-05-21 20:13
 **/
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String email = userRegisterRequest.getEmail();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String code = userRegisterRequest.getCode();
        if (StrUtil.hasBlank(email, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(email, userPassword, checkPassword, code);
        return ResultUtils.success(result);
    }

    /**
     * 获取邮箱验证码（发送）
     * @param emailCodeRequest 验证码请求
     * @param request HTTP请求
     * @return 发送结果
     */
    @PostMapping("/get_emailcode")
    public BaseResponse<String> getEmailCode(@RequestBody EmailCodeRequest emailCodeRequest, HttpServletRequest request) {
        if(emailCodeRequest == null || StrUtil.hasBlank(emailCodeRequest.getEmail(), emailCodeRequest.getType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.sendEmailCode(emailCodeRequest.getEmail(), emailCodeRequest.getType(), request);
        return ResultUtils.success("验证码发送成功");
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求
     * @param request HTTP请求
     * @return 用户视图信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String accountOrEmail = userLoginRequest.getAccountOrEmail();
        String userPassword = userLoginRequest.getUserPassword();
        if (StrUtil.hasBlank(accountOrEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(accountOrEmail, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     * @param request HTTP请求
     * @return 当前登录用户视图信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.isLogin(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户退出登录
     * @param request HTTP请求
     * @return 是否退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 用户注销
     * @param userDestroyRequest 用户注销请求
     * @param request HTTP请求
     * @return 是否注销成功
     */
    @PostMapping("/destroy")
    public BaseResponse<Boolean> userDestroy(@RequestBody DeleteRequest userDestroyRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userDestroyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!loginUser.getId().equals(userDestroyRequest.getId()),
                ErrorCode.NOT_AUTH_ERROR, "只能注销自己的账号");
        userService.asyncDeleteUserData(userDestroyRequest.getId());
        return ResultUtils.success(true);
    }

    /**
     * 添加用户（仅管理员）
     * @param userAddRequest 添加用户请求
     * @return 用户 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码（加密后）
        String encryptPassword = userService.getEncryptPassword(CommonValue.DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        user.setUserName(CommonValue.DEFAULT_USERNAME);
        user.setUserAvatar("https://xiaorui-1350018626.cos.ap-nanjing.myqcloud.com/public/1925200238819598337/2025-05-31_gXMMGtmOoe0Vi9EI.webp");
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户信息（仅管理员）
     * @param id 用户 id
     * @return 用户信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取用户包装信息
     * @param id 用户 id
     * @return 用户包装信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        BaseResponse<User> userResponse = getUserById(id);
        User user = userResponse.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页查询用户信息（仅管理员）
     * @param userQueryRequest 用户查询请求
     * @return 用户分页信息
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 删除用户（仅管理员）
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户头像
     * @param multipartFile 文件
     * @param id 用户 id
     * @param request HTTP请求
     * @return 头像地址
     */
    @PostMapping("/update/avatar")
    public BaseResponse<String> updateUserAvatar(MultipartFile multipartFile,Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        String result = userService.updateUserAvatar(multipartFile,id, request);
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     * @param userModifyPassWord 用户修改密码
     * @param request HTTP请求
     * @return 是否修改成功
     */
    @PostMapping("/changePassword")
    public BaseResponse<Boolean> changePassword(@RequestBody UserModifyPassWord userModifyPassWord, HttpServletRequest request) {
        ThrowUtils.throwIf(userModifyPassWord == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.changePassword(userModifyPassWord, request);
        return ResultUtils.success(result);
    }

    /**
     * 重置密码
     * @param resetPasswordRequest 重置密码请求
     * @return 是否重置成功
     */
    @PostMapping("/reset/password")
    public BaseResponse<Boolean> resetPassword(@RequestBody UserResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String email = resetPasswordRequest.getEmail();
        String newPassword = resetPasswordRequest.getNewPassword();
        String checkPassword = resetPasswordRequest.getCheckPassword();
        String code = resetPasswordRequest.getCode();

        if (StrUtil.hasBlank(email, newPassword, checkPassword, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.resetPassword(email, newPassword, checkPassword, code);
        return ResultUtils.success(result);
    }

    /**
     * 修改绑定邮箱
     * @param userChangeEmailRequest 修改绑定邮箱请求
     * @param request HTTP请求
     * @return 是否修改成功
     */
    @PostMapping("/change/email")
    public BaseResponse<Boolean> changeEmail(@RequestBody UserChangeEmailRequest userChangeEmailRequest, HttpServletRequest request) {
        if (userChangeEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String newEmail = userChangeEmailRequest.getNewEmail();
        String code = userChangeEmailRequest.getCode();
        if (StrUtil.hasBlank(newEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.changeEmail(newEmail, code, request);
        return ResultUtils.success(result);
    }

    /**
     * 更新用户信息
     * @param userUpdateRequest 更新用户信息请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否是管理员，管理员可以更新任意用户，普通用户只能更新自己
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null || !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            userUpdateRequest.setUserRole(UserConstant.DEFAULT_ROLE);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 批量删除用户（仅管理员）
     * @param deleteRequestList 删除请求列表
     * @return 是否删除成功
     */
    @PostMapping("/delete/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteBatchUser(@RequestBody List<DeleteRequest> deleteRequestList) {
        if (CollectionUtils.isEmpty(deleteRequestList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Long> idList = deleteRequestList.stream()
                .map(DeleteRequest::getId)
                .collect(Collectors.toList());
        boolean result = userService.removeByIds(idList);
        return ResultUtils.success(result);
    }

    /**
     * 批量删除用户（仅管理员）
     * @param deleteRequestList 删除请求列表
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/batchDelete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchDeleteUser(@RequestBody List<Long> deleteRequestList, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequestList == null || deleteRequestList.isEmpty(), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<User> pictureList = userService.listByIds(deleteRequestList);
        ThrowUtils.throwIf(pictureList == null || pictureList.isEmpty(), ErrorCode.NOT_FOUND_ERROR);
        boolean result = userService.removeByIds(deleteRequestList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 添加用户签到记录
     * @param request HTTP请求
     * @return 是否签到成功
     */
    @PostMapping("/add/sign_in")
    public BaseResponse<Boolean> addUserSignIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.addUserSignIn(loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取用户签到记录
     * @param year 年份（为空表示当前年份）
     * @param request HTTP请求
     * @return 用户签到记录
     */
    @GetMapping("/get/sign_in")
    public BaseResponse<List<Integer>> getUserSignInRecord(Integer year, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Integer> userSignInRecord = userService.getUserSignInRecord(loginUser.getId(), year);
        return ResultUtils.success(userSignInRecord);
    }

    /**
     * 获取防刷验证码
     * @return 验证码
     */
    @GetMapping("/getcode")
    public BaseResponse<Map<String, String>> getCode() {
        Map<String, String> captchaData = userService.getCaptcha();
        return ResultUtils.success(captchaData);
    }

    /**
     * 用户封禁/解禁（仅管理员）
     * @param request 用户封禁/解禁请求
     * @param httpRequest HTTP请求
     * @return 是否封禁/解禁成功
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banOrUnbanUser(@RequestBody UserUnbanRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User admin = userService.getLoginUser(httpRequest);
        boolean result = userService.banOrUnbanUser(request.getUserId(), request.getIsUnban(), admin);
        return ResultUtils.success(result);
    }


}
