package com.atguigu.gulimall.ware.listener;

import com.atguigu.gulimall.ware.config.MqConfig;
import com.atguigu.gulimall.ware.entity.to.LockReleaseTo;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author dty
 * @date 2022/9/23 11:00
 * 工作单消费者，监听释放队列， 解锁取消或因网络故障等原因未能解锁的库存
 */
@Service
@RabbitListener(queues = MqConfig.X_DEAD_LETTER_ROUTING_KEY)
public class OrderTaskListener {
    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void unLockStock(LockReleaseTo lockReleaseTo, Message message, Channel channel) throws IOException {
        try {
            wareSkuService.unLockStock(lockReleaseTo);
            //解锁成功 ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //出现异常 拒绝ack 消息重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
