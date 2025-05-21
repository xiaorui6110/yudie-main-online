package com.yudie.yudiemainbackend.EmailSendTest;

import com.yudie.yudiemainbackend.utils.EmailSenderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @description:
 * @author: siri
 * @date: 2025-05-21 21:58
 **/
@SpringBootTest
public class EmailSendTest {

    @Resource
    private EmailSenderUtil emailSenderUtil;

    // 测试邮箱（替换为实际可接收邮件的地址）
    private static final String TEST_TO_EMAIL = "shenrui6110@outlook.com";

    /**
     * 测试发送验证码邮件
     */
    @Test
    public void testSendVerificationEmail() {
        // 可随机生成或固定测试值
        String testCode = "123456";
        emailSenderUtil.sendEmail(TEST_TO_EMAIL, testCode);
    }

    /**
     * 测试发送审核通知邮件
     */
    @Test
    public void testSendReviewEmail() {
        String htmlContent = "<h1>您的内容已通过审核</h1><p>详情请登录系统查看。</p>";
        emailSenderUtil.sendReviewEmail(TEST_TO_EMAIL, htmlContent);
    }


}
