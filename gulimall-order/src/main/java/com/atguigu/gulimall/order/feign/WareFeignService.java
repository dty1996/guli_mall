package com.atguigu.gulimall.order.feign;

import com.atguigu.common.to.SkuStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author dty
 * @date 2022/9/19
 * @dec 描述
 */
@FeignClient("wareApp")
public interface WareFeignService {
    @PostMapping("ware/waresku/query/stock")
    List<SkuStockVo> queryStockBySku(@RequestBody List<Long> skuIds);
}
