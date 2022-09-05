package com.atguigu.gulimall.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author Administrator
 *
 */
@Configuration
public class RedissonConfig {
    /**
     * 所有对Redisson的操作都通过redissonClient对象
     *
     * redisson特性：自动落锁 自动延长 阻塞式等待
     *
     *
     * 在加锁时选择指定过期时间，手动解锁（加锁选择指定时间时，不进行自动续费，减少性能消耗）
     *
     *
     * 读写锁 读锁共享锁 写锁互斥锁
     * 读+读：无锁，并发读
     * 写+写：阻塞方式
     * 写+读：等待写锁
     * 读+写：等待读锁
     *
     *
     *
     * @return
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        //单节点
        config.useSingleServer()
                .setPassword("123456")
                .setAddress("redis://106.12.119.213:6379");
        return Redisson.create(config);
    }

}
