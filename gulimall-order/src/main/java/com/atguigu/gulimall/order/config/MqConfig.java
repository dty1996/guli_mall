package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * 消息队列设置
 */
@Configuration
public class MqConfig {


    @RabbitListener(queues = "order.release.queue")
    public void releaseOrder(OrderEntity orderEntity, Channel channel , Message message) throws IOException {
        System.out.println("接受到消息 ====> 时间：" +  new Date()  + "  消息： "+ orderEntity.getOrderSn() + " " + orderEntity.getCreateTime());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 延时队列名称
     */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";

    /**
     * 订单服务交换机（商城系统中每个服务包含一个交换机）
     */
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";


    /**
     * 延迟队列路由键
     */
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create.order";


    /**
     * 死信队列路由键
     */
    public static final String X_DEAD_LETTER_ROUTING_KEY = "order.release.order";

    /**
     * 消息存活时间 1min
     */
    public static final long X_MESSAGE_TTL = 60 * 1000;


    /**
     *订单解锁队列
     */
    private static final String ORDER_RELEASE_QUEUE = "order.release.queue";

    /**
     * 延时队列
     * @return Queue
     */
    @Bean
    public Queue oderDelayQueue() {

        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("x-dead-letter-exchange", ORDER_EVENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", X_DEAD_LETTER_ROUTING_KEY);
        arguments.put("x-message-ttl", X_MESSAGE_TTL);

        return new Queue(ORDER_DELAY_QUEUE, true, false, false, arguments);
    }

    /**
     * 订单解锁队列
     * @return Queue
     */
    @Bean
    public Queue orderReleaseQueue() {

        return new Queue(ORDER_RELEASE_QUEUE, true, false, false);
    }

    /**
     * 订单服务交换机（主题交换机）
     * @return Exchange
     */
    @Bean
    public Exchange orderEventExchange() {

        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false, null);
    }

    /**
     * 订单创建绑定
     * @return Binding
     */
    @Bean
    public Binding orderCreateBinding() {

        return new Binding(ORDER_DELAY_QUEUE, Binding.DestinationType.QUEUE, ORDER_EVENT_EXCHANGE, ORDER_CREATE_ROUTING_KEY, null);
    }

    /**
     * 库存解锁绑定
     * @return Binding
     */
    @Bean
    public Binding orderReleaseBinding() {

        return new Binding(ORDER_RELEASE_QUEUE, Binding.DestinationType.QUEUE, ORDER_EVENT_EXCHANGE, X_DEAD_LETTER_ROUTING_KEY, null);
    }

}
