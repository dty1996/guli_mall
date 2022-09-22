package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.config.MqConfig;
import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author dty
 * @date 2022/9/18
 * @dec 描述
 */
@Controller
public class IndexController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String page(@PathVariable("page") String page) {


        return page;
    }


    @ResponseBody
    @GetMapping("test/order")
    public String testOder() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setCreateTime(new Date());
        rabbitTemplate.convertAndSend(MqConfig.ORDER_EVENT_EXCHANGE, MqConfig.ORDER_CREATE_ROUTING_KEY, orderEntity);
        return "ok";
    }
}
