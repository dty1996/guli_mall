package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReduceTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Administrator
 */
@FeignClient("couponApp")
public interface CouponFeignService {

    @PostMapping("coupon/spubounds/save")
    R  saveBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("coupon/skufullreduction/saveInfo")
    R saveSkuReduceInfo(@RequestBody SkuReduceTo skuLadder);
}
