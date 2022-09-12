package com.atguigu.Controller;

import com.atguigu.common.utils.R;
import com.atguigu.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 发送验证码
 */
@RestController
@RequestMapping
public class SmsController {

    @Autowired
    private SmsComponent smsComponent;

    /**
     * 提供服务给其他的模块
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sms/send")
    public R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendCode(phone, code);
        return R.ok();
    }
}
