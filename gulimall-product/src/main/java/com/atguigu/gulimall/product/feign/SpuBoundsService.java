package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Administrator
 */
@FeignClient("couponApp")
public interface SpuBoundsService {

    @RequestMapping("coupon/spubounds/save")
    R  save(SpuBoundsTo spuBoundsTo);
}
