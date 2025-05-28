package com.yudie.yudiemainbackend.manager.auth.annotation;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * @description: Sa-Token 开启注解和配置
 * @author: siri
 * @date: 2025-05-28 14:34
 * 参考官方文档：<a href="https://sa-token.cc/doc.html#/use/at-check">...</a>
 *             和 <a href="https://sa-token.cc/doc.html#/up/many-account">...</a>
 **/
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

    /**
     * 重写 Sa-Token 的注解处理器，增加注解合并功能
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写 Sa-Token 的注解处理器，增加注解合并功能
        SaAnnotationStrategy.instance.getAnnotation = AnnotatedElementUtils::getMergedAnnotation;
    }
}