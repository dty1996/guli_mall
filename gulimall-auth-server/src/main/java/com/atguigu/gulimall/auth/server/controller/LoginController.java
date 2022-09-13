package com.atguigu.gulimall.auth.server.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.server.constants.AuthServerConstant;
import com.atguigu.gulimall.auth.server.entity.MemberEntity;
import com.atguigu.gulimall.auth.server.entity.params.LoginParam;
import com.atguigu.gulimall.auth.server.entity.params.RegisterParam;
import com.atguigu.gulimall.auth.server.feign.MemberFeignService;
import com.atguigu.gulimall.auth.server.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 描述
 */
@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @ResponseBody
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
     * TODO 重定向携带数据，采用session 分布式session的问题
     * @param registerParam
     * @param bindingResult
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String register(@Valid RegisterParam registerParam, BindingResult bindingResult,RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {

            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            //如果校验出错,重定向到注册页(转发会重复提交表单)
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //校验验证码
        String codeStr = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_PREFIX + registerParam.getPhone());
        if (StringUtils.isNotEmpty(codeStr)) {
            String[] strings = codeStr.split("_");
            if (registerParam.getCode().equals(strings[0])) {
               //校验成功，删除验证码，令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_PREFIX + registerParam.getCode());
            } else {
                Map<String, String> errors = new HashMap<>(1);
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //调用注册接口
        R register = memberFeignService.register(registerParam);
        if (register.getCode() == 0) {
            //成功，跳转登录接口
            return "redirect:http://auth.gulimall.com/";
        } else if (BizExceptionEnum.USER_EXIST_EXCEPTION.getCode().equals(register.getCode())){
            Map<String, String> errors = new HashMap<>(1);
            errors.put("userName", register.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        } else if (BizExceptionEnum.PHONE_EXIST_EXCEPTION.getCode().equals(register.getCode())) {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("phone", register.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        } else {
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }


    @PostMapping("login")
    public String login(LoginParam loginParam, RedirectAttributes redirectAttributes) {

        R login = memberFeignService.login(loginParam);
        if (login.getCode() == 0) {
            MemberEntity member = login.getData(new TypeReference<MemberEntity>() {});
            return "redirect:http://gulimall.com/";
        } else {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("msg", login.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/";
        }

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
