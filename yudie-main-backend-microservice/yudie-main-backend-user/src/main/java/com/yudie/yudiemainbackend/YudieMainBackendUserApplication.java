package com.yudie.yudiemainbackend;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.yudie.yudiemainbackend.mapper")
@ComponentScan("com.yudie")
@EnableDubbo
public class YudieMainBackendUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(YudieMainBackendUserApplication.class, args);
    }

}
