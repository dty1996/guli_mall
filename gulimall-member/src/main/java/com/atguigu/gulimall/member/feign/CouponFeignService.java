package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author dty
 * 远程客户端
 */
@FeignClient("couponApp")
public interface CouponFeignService {

    @RequestMapping("coupon/coupon/member/list")
    R memberCoupon();

}
