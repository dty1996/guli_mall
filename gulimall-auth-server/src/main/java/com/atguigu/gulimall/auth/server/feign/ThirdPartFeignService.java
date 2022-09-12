package com.atguigu.gulimall.auth.server.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 描述
 */
@FeignClient("ThirdPartApp")
public interface ThirdPartFeignService {

    @GetMapping("/sms/send")
    R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
