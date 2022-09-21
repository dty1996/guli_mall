package com.atguigu.gulimall.order.config;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 */
@Configuration
public class FeignClientConfig {

    @Value("${service.feign.connectTimeout:60000}")
    private int connectTimeout;

    @Value("${service.feign.readTimeOut:60000}")
    private int readTimeout;

    @Bean
    public Request.Options options() {
        return new Request.Options(connectTimeout, readTimeout);
    }
}
