package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.entity.to.UserInfoTo;
import com.atguigu.gulimall.cart.thread.UserThreadLocal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 描述
 */
@Controller
public class CartController {

    @GetMapping("cart.html")
    public String cartListPage() {
        UserInfoTo userInfoTo = UserThreadLocal.get();



        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart() {


        return "success";
    }
}
