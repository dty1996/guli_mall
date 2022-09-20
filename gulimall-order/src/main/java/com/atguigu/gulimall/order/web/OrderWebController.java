package com.atguigu.gulimall.order.web;

import com.atguigu.common.to.LoginUserVo;
import com.atguigu.gulimall.order.constants.OrderConstant;
import com.atguigu.gulimall.order.entity.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.entity.vo.OrderItemVo;
import com.atguigu.gulimall.order.entity.vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.entity.vo.SubmitOrderVo;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.thread.UserThreadLocal;
import com.atguigu.gulimall.order.utils.RedisLuaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Administrator
 */
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisLuaUtil redisLuaUtil;

    @GetMapping("toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }


    @PostMapping("submitOrder")
    public String submitOrder(SubmitOrderVo submitOrderVo) {
        SubmitOrderResponseVo submitOrderResponseVo = orderService.submitOrder(submitOrderVo);


        //成功，跳转到支付页面
        return "pay";

    }


}
