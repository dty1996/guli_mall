package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.entity.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Administrator
 */
@FeignClient("cartApp")
public interface CartFeignService {

    @GetMapping("getOrderItem")
    List<OrderItemVo> getOrderItem();
}
