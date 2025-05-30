package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.constant.CommonValue;
import com.yudie.yudiemainbackend.constant.CrawlerConstant;
import com.yudie.yudiemainbackend.constant.RedisConstant;
import com.yudie.yudiemainbackend.constant.UserConstant;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.manager.CrawlerManager;
import com.yudie.yudiemainbackend.manager.FileManager;
import com.yudie.yudiemainbackend.manager.auth.StpKit;
import com.yudie.yudiemainbackend.mapper.UserSignInRecordMapper;
import com.yudie.yudiemainbackend.model.dto.file.UploadPictureResult;
import com.yudie.yudiemainbackend.model.dto.user.UserModifyPassWord;
import com.yudie.yudiemainbackend.model.dto.user.UserQueryRequest;
import com.yudie.yudiemainbackend.model.entity.Picture;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.entity.UserSignInRecord;
import com.yudie.yudiemainbackend.model.enums.UserRoleEnum;
import com.yudie.yudiemainbackend.model.vo.LoginUserVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import com.yudie.yudiemainbackend.service.PictureService;
import com.yudie.yudiemainbackend.service.UserService;
import com.yudie.yudiemainbackend.mapper.UserMapper;
import com.yudie.yudiemainbackend.utils.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBitSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.redisson.api.RedissonClient;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author lenovo
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-21 12:32:41
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService{

    private static final int MIN_USERPASSWORD_LENGTH = 6;

    private static final int MAX_USERPASSWORD_LENGTH = 36;

    private static final int MAX_IPCOUNT = 5;

    private static final int MAX_EMAILCOUNT = 3;

    @Resource
    private  StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailSenderUtil emailSenderUtil;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserSignInRecordMapper userSignInRecordMapper;

    @Lazy
    @Resource
    private CrawlerManager crawlerManager;

    @Resource
    private FileManager fileManager;

    @Lazy
    @Resource
    private PictureService pictureService;

    /**
     * 用户注册
     *
     * @param userEmail 用户邮箱
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param code 验证码
     * @return 用户注册成功后的ID
     */
    @Override
    public long userRegister(String userEmail, String userPassword, String checkPassword, String code) {
        // 1. 校验用户输入的数据
        if (StrUtil.hasBlank(userEmail, userPassword, checkPassword, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        // 校验邮箱、密码
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        if (userPassword.length() < MIN_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (userPassword.length() > MAX_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        // 校验验证码
        String verifyCodeKey = String.format("email:code:verify:register:%s", userEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误或已过期");
        }
        synchronized (userEmail.intern()) {
            // 检查邮箱是否已被注册
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", userEmail);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已被注册");
            }
            // 检查账号是否已被使用
            // 使用邮箱的前缀作为用户账号
            String userAccount = userEmail.substring(0, userEmail.indexOf("@"));
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                // 如果账号已存在，则在账号后添加 4 位随机数
                userAccount = userAccount + RandomUtil.randomNumbers(4);
            }
            // 2. 注册用户
            // 密码加密
            String encryptPassword = DigestUtil.md5Hex(CommonValue.DEFAULT_SALT + userPassword);
            // 保存用户信息
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserEmail(userEmail);
            user.setUserPassword(encryptPassword);
            // 使用用户账号作为默认用户名
            user.setUserName(userAccount);
            user.setUserRole(UserRoleEnum.USER_ROLE_ENUM.getValue());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,"用户注册失败，数据库错误");
            }
            // 删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
            // 3. 返回用户ID
            return user.getId();
        }
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        return SecureUtil.md5(CommonValue.DEFAULT_SALT + userPassword);
    }

    /**
     * 发送邮箱验证码
     * @param userEmail 用户邮箱
     * @param type 验证码类型
     * @param request HTTP请求
     */
    @Override
    public void sendEmailCode(String userEmail, String type, HttpServletRequest request) {
        if (StrUtil.hasBlank(userEmail, type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        // 检测高频操作
        crawlerManager.detectFrequentRequest(request);
        // 检查邮箱格式
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        // 获取客户端 IP
        String clientIp = request.getRemoteAddr();
        String ipKey = String.format("email:code:ip:%s", clientIp);
        String emailKey = String.format("email:code:email:%s", userEmail);
        // 检查 IP 是否频繁请求验证码
        String ipCount = stringRedisTemplate.opsForValue().get(ipKey);
        if (ipCount != null && Integer.parseInt(ipCount)  > MAX_IPCOUNT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_ERROR,"请求验证码过于频繁，请稍后再试");
        }
        // 检查邮箱是否频繁请求验证码
        String emailCount = stringRedisTemplate.opsForValue().get(emailKey);
        if (emailCount != null && Integer.parseInt(emailCount)  > MAX_EMAILCOUNT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_ERROR,"该邮箱请求验证码过于频繁，请稍后再试");
        }
        // 生成 6 位验证码
        String code = RandomUtil.randomNumbers(6);
        // 发送验证码
        try {
            emailSenderUtil.sendEmail(userEmail, code);
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,"发送验证码失败");
        }
        // 记录 IP 和邮箱的请求次数，设置 1 小时过期
        stringRedisTemplate.opsForValue().increment(ipKey, 1);
        stringRedisTemplate.opsForValue().increment(emailKey, 1);
        stringRedisTemplate.expire(ipKey,1, TimeUnit.HOURS);
        stringRedisTemplate.expire(emailKey,1, TimeUnit.HOURS);
        // 将验证码存入 Redis，设置 5 分钟过期
        String verifyCodeKey = String.format("email:code:verify:%s:%s", type, userEmail);
        stringRedisTemplate.opsForValue().set(verifyCodeKey, code, 5, TimeUnit.MINUTES);

    }

    /**
     * 用户登录
     * @param accountOrEmail 账号或邮箱
     * @param userPassword 用户密码
     * @param request HTTP请求
     * @return 登录后的用户信息视图对象（LoginUserVO）
     */
    @Override
    public LoginUserVO userLogin(String accountOrEmail, String userPassword, HttpServletRequest request) {
        // 1.校验用户输入信息
        if (StrUtil.hasBlank(accountOrEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        if (userPassword.length() < MIN_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (userPassword.length() > MAX_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        // 密码加密（用于后续查询）
        String encryptPassword = DigestUtil.md5Hex(CommonValue.DEFAULT_SALT + userPassword);
        // 2.查询用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userPassword", encryptPassword)
                .and( wrapper -> wrapper.eq("userAccount", accountOrEmail)
                        .or()
                        .eq("userEmail", accountOrEmail));
        User user = this.getOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, accountOrEmail cannot match userPassword");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在或密码错误");
        }
        // 3.记录用户登录态
        // 先设置 Sa-Token 登录态，这样可以确保 Session 中有正确的权限信息
        StpKit.SPACE.login(user.getId());
        // 在 Sa-Token Session 中存入完整的用户信息
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 4.返回用户信息
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 验证用户输入的验证码是否正确
     * @param userInputCaptcha 用户输入的验证码
     * @param serverVerifyCode 服务器端存储的加密后的验证码
     * @return 是否正确
     */
    @Override
    public boolean validateCaptcha(String userInputCaptcha, String serverVerifyCode) {
        if (userInputCaptcha != null && serverVerifyCode != null) {
            String encryptedVerifycode = DigestUtil.md5Hex(userInputCaptcha);
            if (encryptedVerifycode.equals(serverVerifyCode)) {
                return true;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
    }

    /**
     * 获取当前登录用户
     * @param request HTTP请求
     * @return 当前登录用户信息（完整信息，包括密码等）
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        try {
            // 优先从 Sa-Token 中获取登录信息
            if (StpKit.SPACE.isLogin()) {
                User user = (User) StpKit.SPACE.getSession().get(UserConstant.USER_LOGIN_STATE);
                if (user != null) {
                    return user;
                }
            }
            // 1.从 Session 中获取登录信息
            Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
            User currentUser = (User) userObj;
            if (currentUser == null || currentUser.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 2.查询数据库获取用户信息
            long userId = currentUser.getId();
            currentUser = this.getById(userId);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 更新 Sa-Token 中的用户信息
            StpKit.SPACE.login(userId);
            StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, currentUser);
            // 3.返回用户信息
            return currentUser;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    /**
     * 判断是否是登录态
     * @param request  HTTP请求
     * @return 是否登录
     */
    @Override
    public User isLogin(HttpServletRequest request) {
        // 1.从 Session 中获取登录信息
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 2.查询数据库获取用户信息
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            return null;
        }
        return currentUser;
    }

    /**
     * 获得脱敏后的登录用户信息
     * @param user 用户
     * @return 脱敏后的登录用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户退出登录
     * @param request HTTP请求
     * @return 是否退出登录
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 1.判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        // 2.移除 Spring Session 登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        // 移除 Sa-Token 登录态
        if (StpKit.SPACE.isLogin()) {
            StpKit.SPACE.logout();
        }
        return true;
    }

    /**
     * 获得脱敏后的用户信息
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR,"请求参数为空");
        // 1.获取查询参数
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 2.构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 修改密码
     * @param userModifyPassWord 用户修改密码
     * @param request HTTP请求
     * @return 是否修改成功
     */
    @Override
    public boolean changePassword(UserModifyPassWord userModifyPassWord, HttpServletRequest request) {
        // 1.校验用户输入信息
        if(StrUtil.hasBlank(userModifyPassWord.getOldPassword(), userModifyPassWord.getNewPassword(), userModifyPassWord.getCheckPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if(!userModifyPassWord.getNewPassword().equals(userModifyPassWord.getCheckPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (userModifyPassWord.getCheckPassword().length() < MIN_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"新密码长度过短");
        }
        if (userModifyPassWord.getNewPassword().length() > MAX_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"新密码长度过长");
        }
        // 2.查询用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userModifyPassWord.getId());
        String encryptPassword = getEncryptPassword(userModifyPassWord.getOldPassword());
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"原密码错误");
        }
        // 3.修改密码
        user.setUserPassword(getEncryptPassword(userModifyPassWord.getNewPassword()));
        int updateResult = userMapper.updateById(user);

        // TODO 更新 ES

        // 4.返回修改结果
        return updateResult > 0;
    }

    /**
     * 判断是否是管理员
     * @param user 登录用户
     * @return 是否是管理员
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN_ROLE_ENUM.getValue().equals(user.getUserRole());
    }

    /**
     * 修改用户头像
     * @param multipartFile 文件
     * @param id 用户 id
     * @param request HTTP请求
     * @return 上传图片的 URL
     */
    @Override
    public String updateUserAvatar(MultipartFile multipartFile, Long id, HttpServletRequest request) {
        // 判断用户是否存在
        User user = userMapper.selectById(id);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 判断用户是否登录
        User loginUser = getLoginUser(request);
        if(loginUser == null || !loginUser.getId().equals(id)){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        // 判断文件是否为空
        if(multipartFile == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        // 判断文件类型
        // 上传图片，得到图片信息
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("avatars/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 更新用户头像
        user.setUserAvatar(uploadPictureResult.getUrl());
        // 更新MySQL
        userMapper.updateById(user);

        // TODO 更新ES

        return uploadPictureResult.getUrl();
    }

    /**
     * 获取验证码
     * @return 验证码
     */
    @Override
    public Map<String, String> getCaptcha() {
        // 仅包含数字的字符集
        String characters = "0123456789";
        // 生成 4 位数字验证码
        RandomGenerator randomGenerator = new RandomGenerator(characters, 4);
        // 定义图片的显示大小，并创建验证码对象
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(320, 100, 4, 4);
        shearCaptcha.setGenerator(randomGenerator);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        shearCaptcha.write(outputStream);
        byte[] captchaBytes = outputStream.toByteArray();
        String base64Captcha = Base64.getEncoder().encodeToString(captchaBytes);
        String captchaCode = shearCaptcha.getCode();
        // 使用 Hutool 的 MD5 加密
        String encryptedCaptcha = DigestUtil.md5Hex(captchaCode);
        // 将加密后的验证码和 Base64 编码的图片存储到 Redis 中，设置过期时间为 5 分钟
        stringRedisTemplate.opsForValue().set("captcha:" + encryptedCaptcha, captchaCode, 300, TimeUnit.SECONDS);
        Map<String, String> data = new HashMap<>();
        data.put("base64Captcha", base64Captcha);
        data.put("encryptedCaptcha", encryptedCaptcha);
        return data;
    }

    /**
     * 修改邮箱
     * @param newEmail 新邮箱
     * @param code 验证码
     * @param request HTTP请求
     * @return 修改邮箱结果
     */
    @Override
    public boolean changeEmail(String newEmail, String code, HttpServletRequest request) {
        // 1.校验用户输入信息
        if (StrUtil.hasBlank(newEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        if (!newEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        // 2.校验验证码
        String verifyCodeKey = String.format("email:code:verify:changeEmail:%s", newEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误或已过期");
        }
        // 3.查询用户信息
        User loginUser = getLoginUser(request);
        synchronized (newEmail.intern()) {
            // 检查新邮箱是否已被使用
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", newEmail);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已被使用");
            }
            // 4.修改邮箱
            User user = new User();
            user.setId(loginUser.getId());
            user.setUserEmail(newEmail);
            boolean updateResult = this.updateById(user);
            if (!updateResult) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,"修改邮箱失败");
            }
            // 5.删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
            // 6.返回修改结果
            return true;
        }
    }

    /**
     * 重置密码（忘记密码的情况下）
     * @param userEmail 用户邮箱
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @param code 验证码
     * @return 重置密码结果
     */
    @Override
    public boolean resetPassword(String userEmail, String newPassword, String checkPassword, String code) {
        // 1.校验用户输入信息
        if (StrUtil.hasBlank(userEmail, newPassword, checkPassword, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        if (newPassword.length() < MIN_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (newPassword.length() > MAX_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        // 2.校验验证码
        String verifyCodeKey = String.format("email:code:verify:changeEmail:%s", userEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误或已过期");
        }
        // 3.查询用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userEmail", userEmail);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 4.修改密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(getEncryptPassword(newPassword));
        boolean updateResult = this.updateById(updateUser);
        if (updateResult) {
            // 5.删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
        }
        // 6.返回修改结果
        return updateResult;

    }

    /**
     * 封禁或解禁用户
     * @param userId 目标用户 id
     * @param isUnban true - 解禁，false - 封禁
     * @param admin 执行操作的管理员
     * @return 封禁或解禁结果
     */
    @Override
    public boolean banOrUnbanUser(Long userId, Boolean isUnban, User admin) {
        // 1.校验用户输入信息
        if (userId == null || userId <= 0 || isUnban == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.校验管理员权限
        if (!UserConstant.ADMIN_ROLE.equals(admin.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "非管理员不能执行此操作");
        }
        // 3.获取用户信息
        User targetUser = this.getById(userId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 4.检查当前状态是否需要变更
        boolean isBanned = CrawlerConstant.BAN_ROLE.equals(targetUser.getUserRole());
        // 5.变更用户状态
        if (isUnban == isBanned) {
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setUserRole(isUnban ? UserConstant.DEFAULT_ROLE : CrawlerConstant.BAN_ROLE);
            updateUser.setUpdateTime(new Date());
            boolean result = this.updateById(updateUser);
            if (result) {
                // 6. 记录操作日志
                log.info("管理员[{}]{}用户[{}]",
                        admin.getUserAccount(),
                        isUnban ? "解封" : "封禁",
                        targetUser.getUserAccount());

                // 7. 处理Redis缓存
                String banKey = String.format("user:ban:%d", userId);
                if (isUnban) {
                    stringRedisTemplate.delete(banKey);
                } else {
                    stringRedisTemplate.opsForValue().set(banKey, "1");
                }

                // TODO 8. 更新ES中的用户信息

            }

            return result;
        } else {
            // 状态已经是目标状态
            String operation = isUnban ? "解封" : "封禁";
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    String.format("该用户当前%s不需要%s", isUnban ? "未被封禁" : "已被封禁", operation));
        }
    }

    /**
     * 异步删除用户数据
     * @param userId 目标用户 id
     */
    @Override
    @Async("asyncExecutor")
    public void asyncDeleteUserData(Long userId) {
        try {
            // 1. 删除用户发布的图片
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.eq("userId", userId);
            List<Picture> pictureList = pictureService.list(pictureQueryWrapper);
            if (!pictureList.isEmpty()) {
                // 删除数据库记录
                pictureService.remove(pictureQueryWrapper);

                // TODO 删除ES中的图片记录

            }

            // TODO 2. 删除用户发布的帖子


            // 3. 删除用户数据
            this.removeById(userId);

            // TODO 删除ES中的用户记录

            // 4. 清理相关缓存
            String userKey = String.format("user:ban:%d", userId);
            stringRedisTemplate.delete(userKey);

            log.info("用户相关数据删除完成, userId={}", userId);
        } catch (Exception e) {
            log.error("删除用户相关数据失败, userId={}", userId, e);
            // 这里不抛出异常，因为是异步操作，主流程已经完成
        }

    }

    /**
     * 添加用户签到记录
     * @param userId 用户 id
     * @return 当前用户是否已签到成功
     */
    @Override
    public boolean addUserSignIn(long userId) {
        LocalDate date = LocalDate.now();
        int currentYear = date.getYear();
        String redisKey = RedisConstant.getUserSignInRedisKey(currentYear, userId);
        // 获取 Redis 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(redisKey);
        int daOfYear = date.getDayOfYear();
        // 1.判断用户是否已签到
        if (!signInBitSet.get(daOfYear)) {
            // 2.如果当前未签到，则签到
            signInBitSet.set(daOfYear, true);
            // 设置 Redis 键的过期时间到当年最后一天
            LocalDate lastDayOfYear = LocalDate.of(currentYear, 12, 31);
            Duration timeUntilEndOfYear = Duration.between(
                    LocalDateTime.now(),
                    lastDayOfYear.atTime(23, 59, 59)
            );
            redissonClient.getBucket(redisKey).expire(timeUntilEndOfYear);
        }
        // 3.返回签到结果
        return true;
    }

    /**
     * 获取用户某个年份的签到记录
     * @param userId 用户 id
     * @param year 年份（为空表示当前年份）
     * @return 用户签到记录
     */
    @Override
    public List<Integer> getUserSignInRecord(long userId, Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        int currentYear = LocalDate.now().getYear();
        List<Integer> signInDays = new ArrayList<>();
        if (year != currentYear) {
            // 1.如果不是当前年份，则直接从数据库查询
            QueryWrapper<UserSignInRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId)
                    .eq("year", year);
            UserSignInRecord record  = userSignInRecordMapper.selectOne(queryWrapper);
            if (record != null && record.getSignInData()!= null) {
                byte[] signInData = record.getSignInData();
                // 2.解析 bitmap 数据
                final int YEAR_OF_DAYS = 366;
                for (int day = 1; day <= YEAR_OF_DAYS; day++) {
                    int byteIndex = (day - 1) / 8;
                    int bitIndex = (day - 1) % 8;
                    if ((signInData[byteIndex] & (1 << bitIndex)) != 0) {
                        signInDays.add(day);
                    }
                }
            }
            return signInDays;
        }
        // 3.当年数据则从 Redis 中获取
        String redisKey = RedisConstant.getUserSignInRedisKey(year, userId);
        RBitSet signInBitSet = redissonClient.getBitSet(redisKey);
        // 如果 Redis 中没有数据，则从数据库中查询
        if (!signInBitSet.isExists()) {
            QueryWrapper<UserSignInRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId)
                    .eq("year", year);
            UserSignInRecord record  = userSignInRecordMapper.selectOne(queryWrapper);
            if (record != null && record.getSignInData()!= null) {
                byte[] signInData = record.getSignInData();
                // 解析 bitmap 数据
                final int YEAR_OF_DAYS = 366;
                for (int day = 1; day <= YEAR_OF_DAYS; day++) {
                    int byteIndex = (day - 1) / 8;
                    int bitIndex = (day - 1) % 8;
                    if ((signInData[byteIndex] & (1 << bitIndex)) != 0) {
                        signInDays.add(day);
                    }
                }
                // 设置过期时间到年底
                LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);
                Duration timeUntilEndOfYear = Duration.between(
                        LocalDateTime.now(),
                        lastDayOfYear.atTime(23, 59, 59)
                );
                redissonClient.getBucket(redisKey).expire(timeUntilEndOfYear);
            }
        }
        // 4.返回签到记录
        // 从 Redis 的 bitmap 中获取签到记录
        BitSet bitSet = signInBitSet.asBitSet();
        int index = bitSet.nextSetBit(0);
        while (index >= 0) {
            signInDays.add(index);
            index = bitSet.nextSetBit(index + 1);
        }
        return signInDays;
    }

    // 重写 MyBatis Plus 的常用方法，主要是加上 ES 的同步操作

    /**
     * 根据主键ID从MySQL删除
     * @param id 主键ID
     * @return 结果
     */
    @Override
    public boolean removeById(Serializable id) {
        // 从MySQL删除
        boolean result = super.removeById(id);

            // TODO 从ES删除

        return result;
    }

    /**
     * 从MySQL批量删除
     * @param idList 主键ID或实体列表
     * @return 结果
     */
    @Override
    public boolean removeByIds(Collection<?> idList) {
        // 从 MySQL 批量删除
        boolean result = super.removeByIds(idList);

            // TODO 从ES批量删除

        return result;
    }

    /**
     * 更新 MySQL
     * @param entity 实体对象
     * @return 结果
     */
    @Override
    public boolean updateById(User entity) {
        // 更新 MySQL
        boolean result = super.updateById(entity);

        // TODO 转换为ES实体

        return result;
    }


}