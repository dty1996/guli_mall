package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.entity.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Administrator
 */
@FeignClient("memberApp")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/getAddress")
    List<MemberAddressVo>  getMemberAddress(@RequestParam Long userId);


}
