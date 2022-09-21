package com.atguigu.gulimall.order.feign;

import com.atguigu.common.to.SkuStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.entity.to.WareSkuLockTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping("ware/wareinfo/fare")
    R getFareByAddress(@RequestParam("addrId") Long addrId);


    @PostMapping("ware/waresku/lock")
    R lockWare(@RequestBody WareSkuLockTo wareSkuLockTo);

}
