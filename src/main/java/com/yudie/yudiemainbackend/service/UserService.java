package com.yudie.yudiemainbackend.service;

import com.yudie.yudiemainbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author lenovo
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-21 12:32:41
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userEmail 用户邮箱
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param code 验证码
     * @return 新用户 id
     */
    long userRegister(String userEmail, String userPassword, String checkPassword, String code);


    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 发送邮箱验证码
     * @param userEmail 用户邮箱
     * @param type 验证码类型
     * @param request HTTP请求
     */
    void sendEmailCode(String userEmail, String type, HttpServletRequest request);




}
