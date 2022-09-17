package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.entity.vo.Cart;
import com.atguigu.gulimall.cart.entity.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @author dty
 * @date 2022/9/17
 * @dec 描述
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer count);

    void deleteItem(Long skuId);
}
