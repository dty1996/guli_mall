package com.atguigu.gulimall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dty
 * @date 2022/9/14
 * @dec 描述
 */
@FeignClient("productApp")
public interface ProductFeignService {
}
