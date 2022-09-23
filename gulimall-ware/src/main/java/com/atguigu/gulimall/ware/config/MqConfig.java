package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dty
 * @date 2022/9/23 9:04
 * 每锁定一个sku库存要发送消息到mq通知解锁。
 * 解锁消费者需判端订单状态和库存状态：
 * 订单如果没创建成功或者取消了，需解锁库存（消息中保存了sku扣库存的信息、在订单保存失败后，操作数据库进行释放锁定库存）
 * 所有消息设置手动确认、消费失败后重新入队
 */
@Configuration
public class MqConfig {


    /**
     * 库存延时队列名称
     */
    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";

    /**
     * 库存服务交换机（商城系统中每个服务包含一个交换机）
     */
    public static final String STOCK_EVENT_EXCHANGE = "stock-event-exchange";


    /**
     * 延迟队列路由键
     */
    public static final String STOCK_CREATE_ROUTING_KEY = "stock.delay.stock";


    /**
     * 死信队列路由键
     */
    public static final String X_DEAD_LETTER_ROUTING_KEY = "stock.release.stock";

    /**
     * 消息存活时间 2s
     */
    public static final long X_MESSAGE_TTL = 2 * 1000;


    /**
     *  库存解锁队列
     */
    private static final String STOCK_RELEASE_QUEUE = "stock.release.queue";

    /**
     * 延时队列
     * @return Queue
     */
    @Bean
    public Queue oderDelayQueue() {

        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("x-dead-letter-exchange", STOCK_EVENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", X_DEAD_LETTER_ROUTING_KEY);
        arguments.put("x-message-ttl", X_MESSAGE_TTL);

        return new Queue(STOCK_DELAY_QUEUE, true, false, false, arguments);
    }

    /**
     * 订单解锁队列
     * @return Queue
     */
    @Bean
    public Queue stockReleaseQueue() {

        return new Queue(STOCK_RELEASE_QUEUE, true, false, false);
    }

    /**
     * 订单服务交换机（主题交换机）
     * @return Exchange
     */
    @Bean
    public Exchange stockEventExchange() {

        return new TopicExchange(STOCK_EVENT_EXCHANGE, true, false, null);
    }

    /**
     * 库存绑定路由键
     * @return Binding
     */
    @Bean
    public Binding stockDelayBinding() {

        return new Binding(STOCK_DELAY_QUEUE, Binding.DestinationType.QUEUE, STOCK_EVENT_EXCHANGE, STOCK_CREATE_ROUTING_KEY, null);
    }

    /**
     * 库存解锁绑定
     * @return Binding
     */
    @Bean
    public Binding stockReleaseBinding() {

        return new Binding(STOCK_RELEASE_QUEUE, Binding.DestinationType.QUEUE, STOCK_EVENT_EXCHANGE, X_DEAD_LETTER_ROUTING_KEY, null);
    }
}
