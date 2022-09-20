package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.entity.to.SpuInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author dty
 * @date 2022/9/20
 * @dec 描述
 */
@FeignClient("productApp")
public interface ProductFeignService {
    @GetMapping("getSpuInfosBySkuIds")
    Map<Long, SpuInfoEntity> getSpuInfosBySkuIds(@RequestParam List<Long> skuIds);
}