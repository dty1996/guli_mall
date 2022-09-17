package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.entity.to.UserInfoTo;
import com.atguigu.gulimall.cart.entity.vo.Cart;
import com.atguigu.gulimall.cart.entity.vo.CartItem;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.thread.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 描述
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("cart.html")
    public String cartListPage(Model model) {

        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }


    /**
     * 是否勾选
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);

        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @GetMapping("countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer count) {
        cartService.countItem(skuId, count);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @GetMapping("deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {

        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 重定向到成功页面，防止刷新重复添加
     * @param skuId
     * @param num
     * @param redirectAttributes
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess";
    }


    @GetMapping ("/addToCartSuccess")
    public String success(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("items", cartItem);
        return "success";
    }



}
