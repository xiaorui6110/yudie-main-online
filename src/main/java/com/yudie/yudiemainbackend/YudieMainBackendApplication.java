package com.yudie.yudiemainbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author siri
 */
@SpringBootApplication
@MapperScan("com.yudie.yudiemainbackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YudieMainBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YudieMainBackendApplication.class, args);
    }

}
