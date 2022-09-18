package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author dty
 * @date 2022/9/18
 * @dec 描述
 */
@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("send")
    public String sendMessage() {

        for (int i = 0; i < 10; i++) {
            OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
            entity.setId(1L);
            entity.setName("消息" + i);
            entity.setCreateTime(new Date());
            entity.setStatus(0);
            entity.setSort(1);
            rabbitTemplate.convertAndSend("hello.java", "hello.java", entity);
        }

        return "ok";
    }
}
