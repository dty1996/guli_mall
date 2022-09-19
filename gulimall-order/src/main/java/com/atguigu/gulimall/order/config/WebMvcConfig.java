package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.intercepter.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author dty
 * @date 2022/9/15
 * @dec mvc配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截除支付接口外的所有接口
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/order/pay/**");
    }
}
