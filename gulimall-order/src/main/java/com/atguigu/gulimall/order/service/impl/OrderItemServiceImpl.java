package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;



@RabbitListener(queues = {"hello.java.queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity orderReturnReasonEntity,
                               Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (deliveryTag % 2 == 0) {
            try {
                channel.basicAck(deliveryTag, false);
                System.out.println("接收到消息" + orderReturnReasonEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

//    @RabbitHandler
//    public void receiveMessage(Message message,
//                               OrderReturnReasonEntity orderReturnReasonEntity) {
//        System.out.println("接收到消息" + orderReturnReasonEntity);
//    }

}
