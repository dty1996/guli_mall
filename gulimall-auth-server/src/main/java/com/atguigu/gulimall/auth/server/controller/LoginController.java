package com.atguigu.gulimall.auth.server.controller;

import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.server.constants.AuthServerConstant;
import com.atguigu.gulimall.auth.server.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 描述
 */
@RestController
@RequestMapping
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        ////TODO 1.接口防刷

        String code = generateCode();

        String innerVal = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_PREFIX + phone);
        //频率问题
        if (StringUtils.isNotEmpty(innerVal)) {
            String[] strings = innerVal.split("_");
            long time = Long.valueOf(strings[1]);
            if (System.currentTimeMillis() - time < 60 * 1000) {
                return R.error(BizExceptionEnum.SMS_CODE_EXCEPTION.getCode(), BizExceptionEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //2.接口防刷再次校验, 存入redis中
        String value = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_PREFIX + phone, value, 15 , TimeUnit.MINUTES);
        thirdPartFeignService.sendSmsCode(phone, code);
        return R.ok();
    }


    /**
     * 生成随机6位验证码
     * @return
     */
    private  String generateCode() {
        long codeL = System.nanoTime();
        String codeStr = Long.toString(codeL);
        return codeStr.substring(codeStr.length() - 6);
    }


}
