package com.yudie.yudiemainbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author xiaorui
 */
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.yudie.yudiemainbackend.mapper")
public class YudieMainBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YudieMainBackendApplication.class, args);
    }

}
