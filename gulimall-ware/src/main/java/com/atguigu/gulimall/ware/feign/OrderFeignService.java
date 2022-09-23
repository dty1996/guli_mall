package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author dty
 * @date 2022/9/23 11:21
 */
@FeignClient("orderApp")
public interface OrderFeignService {
    @RequestMapping("order/order/getOrderByOrderSn/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn")String orderSn);
}
