package com.atguigu.gulimall.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Administrator
 *
 */
@Configuration
public class RedissonConfig {
    /**
     * 所有对Redisson的操作都通过redissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        //单节点
        config.useSingleServer()
                .setPassword("123456")
                .setAddress("redis://106.12.119.213:6379");
        return Redisson.create(config);
    }

}
