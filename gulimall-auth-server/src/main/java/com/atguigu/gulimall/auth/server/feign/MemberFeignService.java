package com.atguigu.gulimall.auth.server.feign;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.server.entity.params.LoginParam;
import com.atguigu.gulimall.auth.server.entity.params.RegisterParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("memberApp")
public interface MemberFeignService {

    @PostMapping("member/member/register")
    R register(@RequestBody RegisterParam registerParam) ;

    @PostMapping("member/member/login")
    R login(@RequestBody LoginParam loginParam) ;

}
