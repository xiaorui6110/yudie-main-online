package com.yudie.yudiemainbackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudie.yudiemainbackend.constant.CommonValue;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.model.enums.UserRoleEnum;
import com.yudie.yudiemainbackend.service.UserService;
import com.yudie.yudiemainbackend.mapper.UserMapper;
import com.yudie.yudiemainbackend.utils.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
* @author lenovo
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-21 12:32:41
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    private final int MIN_USERPASSWORD_LENGTH = 6;

    private final int MAX_USERPASSWORD_LENGTH = 24;

    private final int MAX_IPCOUNT = 5;

    private final int MAX_EMAILCOUNT = 3;

    @Resource
    private  StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailSenderUtil emailSenderUtil;

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
        if (userPassword.length() < MIN_USERPASSWORD_LENGTH || userPassword.length() > MAX_USERPASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短或过长");
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
        // TODO  检测高频操作 crawlerManager
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










}


