package com.atguigu.gulimall.auth.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 描述
 */
@EnableDiscoveryClient
@EnableFeignClients(value = "com.atguigu.gulimall.auth.server.feign")
@SpringBootApplication
public class AuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}