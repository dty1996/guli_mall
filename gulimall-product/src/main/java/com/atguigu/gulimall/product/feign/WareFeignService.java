package com.atguigu.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author dty
 * @date 2022/8/31
 * @dec 描述
 */
@FeignClient("wareApp")
public interface WareFeignService {

    @RequestMapping("{skuId}/hasStock")
    Boolean hasStock(@PathVariable("skuId") Long skuId) ;

}
