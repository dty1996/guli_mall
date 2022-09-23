package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dty
 * @date 2022/9/23 9:01
 */
@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter messageConverter () {
        return new Jackson2JsonMessageConverter();
    }

}
