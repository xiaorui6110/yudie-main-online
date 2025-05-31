package com.yudie.yudiemainbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yudie.yudiemainbackend.model.dto.user.UserModifyPassWord;
import com.yudie.yudiemainbackend.model.dto.user.UserQueryRequest;
import com.yudie.yudiemainbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudie.yudiemainbackend.model.vo.LoginUserVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
* @author xiaorui
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-21 12:32:41
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param email 用户邮箱
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param code 验证码
     * @return 新用户 id
     */
    long userRegister(String email, String userPassword, String checkPassword, String code);

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 发送邮箱验证码
     * @param email 用户邮箱
     * @param type 验证码类型
     * @param request HTTP请求
     */
    void sendEmailCode(String email, String type, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param accountOrEmail 账号或邮箱
     * @param userPassword 用户密码
     * @param request HTTP请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String accountOrEmail, String userPassword, HttpServletRequest request);

    /**
     * 验证用户输入的验证码是否正确
     *
     * @param userInputCaptcha 用户输入的验证码
     * @param serverVerifyCode 服务器端存储的加密后的验证码
     * @return 如果验证成功返回 true，否则返回 false
     */
    boolean validateCaptcha(String userInputCaptcha, String serverVerifyCode);

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求
     * @return 当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断是否是登录态
     * @param request  HTTP请求
     * @return 是否登录
     */
    User isLogin(HttpServletRequest request);

    /**
     * 获得脱敏后的登录用户信息
     * @param user 用户
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户退出登录
     *
     * @param request HTTP请求
     * @return 是否退出登录
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获得脱敏后的用户信息
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获得脱敏后的用户信息列表
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 修改密码
     * @param userModifyPassWord 用户修改密码
     * @param request HTTP请求
     * @return 是否修改成功
     */
    boolean changePassword(UserModifyPassWord userModifyPassWord, HttpServletRequest request);

    /**
     * 判断是否是管理员
     * @param user 登录用户
     * @return 是否是管理员
     */
    boolean isAdmin(User user);

    /**
     * 修改用户头像
     * @param multipartFile 文件
     * @param id 用户 id
     * @param request HTTP请求
     * @return 上传图片的 URL
     */
    String updateUserAvatar(MultipartFile multipartFile, Long id, HttpServletRequest request);

    /***
     * 获取验证码
     * @return 验证码
     */
    Map<String, String> getCaptcha();

    /***
     * 修改绑定邮箱
     * @param newEmail 新邮箱
     * @param code 验证码
     * @param request HTTP请求
     * @return 是否修改成功
     */
    boolean changeEmail(String newEmail, String code, HttpServletRequest request);

    /**
     * 重置密码
     * @param email 用户邮箱
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @param code 验证码
     * @return 是否重置成功
     */
    boolean resetPassword(String email, String newPassword, String checkPassword, String code);

    /**
     * 封禁或解禁用户
     * @param userId 目标用户 id
     * @param isUnban true - 解禁，false - 封禁
     * @param admin 执行操作的管理员
     * @return 是否操作成功
     */
    boolean banOrUnbanUser(Long userId, Boolean isUnban, User admin);

    /**
     * 删除用户数据
     * @param userId 目标用户 id
     */
    void asyncDeleteUserData(Long userId);

    /**
     * 添加用户签到记录
     * @param userId 用户 id
     * @return 当前用户是否已签到成功
     */
    boolean addUserSignIn(long userId);

    /**
     * 获取用户某个年份的签到记录
     * @param userId 用户 id
     * @param year 年份（为空表示当前年份）
     * @return 签到记录
     */
    List<Integer> getUserSignInRecord(long userId, Integer year);

}
