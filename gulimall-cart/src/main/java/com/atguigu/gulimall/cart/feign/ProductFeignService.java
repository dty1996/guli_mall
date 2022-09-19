package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.entity.vo.NewSkuPriceVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author dty
 * @date 2022/9/14
 * @dec 描述
 */
@FeignClient("productApp")
public interface ProductFeignService {

    @RequestMapping("product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("product/skusaleattrvalue/getSkuAttrValuesString/{skuId}")
    List<String> getSkuAttrValuesString(@PathVariable("skuId") Long skuId);

    @RequestMapping("product/skuinfo/getNewSkuPrice")
    List<NewSkuPriceVo> getNewSkuPrice(@RequestParam("skuIds") List<Long> skuIds);
}
