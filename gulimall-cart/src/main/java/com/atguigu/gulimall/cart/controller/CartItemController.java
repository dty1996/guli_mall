package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.entity.vo.OrderItemVo;
import com.atguigu.gulimall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Administrator
 */
@RestController
@RequestMapping
public class CartItemController {

    @Autowired
    private CartService cartService;

    @GetMapping("getOrderItem")
    public List<OrderItemVo> getOrderItem() {
       return cartService.getOrderItem();
    }

}
