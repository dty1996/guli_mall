package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author dty
 * @date 2022/9/18
 * @dec 描述
 */
@Configuration
public class RabbitmqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * JSON 序列化
     * @return
     */
    @Bean
    public MessageConverter messageConverter () {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * config对象创建完成后，执行方法
     *
     * 定制RabbitTemplate
     * 1.服务端(broker)收到消息回调
     *      1.设置publisher-confirms =true
     *      2.消息正确抵达队列进行回溯
     * 2.消息到达队列后进行回调
     *      1.设置publisher-return-true
     *      2.设置确认回调ReturnCallBack
     * 3.消息端收到消息默认自动ack
     *      消费端宕机，消息会丢失
     */


    @PostConstruct
    public void initRabbitTemplate() {
        //接受消息设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达Broker就 ack=true
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("消息" +  correlationData + "==>" + "[ack=]" + ack +  "[cause=]" + cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * Returned message callback.
             * @param message the returned message.
             * @param replyCode the reply code.
             * @param replyText the reply text.
             * @param exchange the exchange.
             * @param routingKey the routing key.
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("消息发送队列失败" + message + " replyCode:" + replyCode + " replyTex:" + replyText +   " exchange:" + exchange + " routingKey" + routingKey);
            }

        });
    }
}
