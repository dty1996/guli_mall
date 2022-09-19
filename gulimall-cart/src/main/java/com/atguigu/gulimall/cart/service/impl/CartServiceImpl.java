package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SkuInfoTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.entity.to.UserInfoTo;
import com.atguigu.gulimall.cart.entity.vo.Cart;
import com.atguigu.gulimall.cart.entity.vo.CartItem;
import com.atguigu.gulimall.cart.entity.vo.NewSkuPriceVo;
import com.atguigu.gulimall.cart.entity.vo.OrderItemVo;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.thread.UserThreadLocal;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author dty
 * @date 2022/9/17
 * @dec 描述
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private ProductFeignService productFeignService;


    public static final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> hashOperations = getOptions();
        Object o = hashOperations.get(skuId.toString());
        if (null != o && StringUtils.isNotEmpty(o.toString())) {
            String jsonString = o.toString();
            CartItem cartItem = JSON.parseObject(jsonString, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            String cartItemStr = JSON.toJSONString(cartItem);
            hashOperations.put(skuId.toString(), cartItemStr);
            return cartItem;
        } else {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                //查询sku信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoTo infoTo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoTo>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(infoTo.getSkuDefaultImg());
                cartItem.setPrice(infoTo.getPrice());
                cartItem.setTitle(infoTo.getSkuTitle());
            });

            CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(() -> {
                List<String> skuAttrValuesString = productFeignService.getSkuAttrValuesString(skuId);
                cartItem.setSkuAttr(skuAttrValuesString);
            });

            //阻塞等待异步任务完成
            CompletableFuture.allOf(skuInfoFuture, skuAttrFuture).get();
            String jsonString = JSONObject.toJSONString(cartItem);

            hashOperations.put(skuId.toString(), jsonString);

            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOperations = getOptions();
        String toString = Objects.requireNonNull(hashOperations.get(skuId.toString())).toString();
        return  JSON.parseObject(toString, CartItem.class);

    }

    /**
     * 获取购物车对象
     * @return
     */

    @Override
    public Cart getCart() {
        UserInfoTo userInfoTo = UserThreadLocal.get();
        String userKey = userInfoTo.getUserKey();
        List<CartItem> temCartItems = getCartItemsFromRedis(userKey);

        Cart cart = new Cart();
        if (null != userInfoTo.getUserId()) {
            //登录用户
            BoundHashOperations<String, Object, Object> hashOperations = getOptions();
            if (temCartItems != null && temCartItems.size() > 0) {
                for (CartItem cartItem : temCartItems) {
                    Object o = hashOperations.get(cartItem.getSkuId().toString());

                    if (null != o && StringUtils.isNotEmpty(o.toString())) {
                        //购物车中有此商品 加上这次的数量
                        CartItem parseObject = JSON.parseObject(o.toString(), CartItem.class);
                        parseObject.setCount(parseObject.getCount() + cartItem.getCount());
                        hashOperations.put(cartItem.getSkuId().toString(), JSON.toJSONString(parseObject));
                    } else {
                        //否则加入新商品项
                        hashOperations.put(cartItem.getSkuId().toString(), JSON.toJSONString(cartItem));
                    }
                }
            }
            //从redis中取出数据
            List<CartItem> cartItems = getCartItemsFromRedis(userInfoTo.getUserId().toString());
            cart.setItems(cartItems);
            //删除临时购物车
            stringRedisTemplate.delete(CART_PREFIX + userKey);
        } else {
            //临时用户
            cart.setItems(temCartItems);
        }
        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItemBySkuId(skuId);
        cartItem.setCheck(check == 1);
        BoundHashOperations<String, Object, Object> hashOperations = getOptions();
        hashOperations.put(skuId.toString(), JSON.toJSONString(cartItem));
    }


    @Override
    public void countItem(Long skuId, Integer count) {
        CartItem cartItem = getCartItemBySkuId(skuId);
        cartItem.setCount(count);
        BoundHashOperations<String, Object, Object> hashOperations = getOptions();
        hashOperations.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOperations = getOptions();
        hashOperations.delete(skuId.toString());
    }

    private  BoundHashOperations<String, Object, Object>  getOptions() {
        UserInfoTo userInfoTo = UserThreadLocal.get();
        //登录用户
        String cartKey ;
        if (null != userInfoTo.getUserId()) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //购物车数据存在redis中采用hash的数据结构
        return stringRedisTemplate.opsForHash().getOperations().boundHashOps(cartKey);
    }


    @Override
    public List<OrderItemVo> getOrderItem(Long userId) {
        List<CartItem> cartItems = getCartItemsFromRedis(userId.toString());
        List<Long> skuIds = cartItems.stream().map(CartItem::getSkuId).collect(Collectors.toList());
        //获取最新的价格
        List<NewSkuPriceVo> newSkuPriceVos = productFeignService.getNewSkuPrice(skuIds);

        //list转为map
        Map<Long, BigDecimal> priceMap = newSkuPriceVos.stream().collect(Collectors.toMap(NewSkuPriceVo::getSkuId, NewSkuPriceVo::getPrice));
        //从购物车中选择选中的购物项放入订单中
        return cartItems.stream().filter(CartItem::getCheck).map(cartItem -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            BeanUtils.copyProperties(cartItem, orderItemVo);
            //获得最新的价格
            orderItemVo.setPrice(priceMap.get(orderItemVo.getSkuId()));
            return orderItemVo;
        }).collect(Collectors.toList());

    }

    /**
     * 获取购物车购物项
     * @return
     */
    private List<CartItem> getCartItemsFromRedis(String itemKey) {
        String key = CART_PREFIX + itemKey;
        BoundHashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash().getOperations().boundHashOps(key);
        Set<Object> keys = hashOperations.keys();
        List<CartItem> items = new ArrayList<>();

        if (keys!=null && keys.size() > 0) {
             items = keys.stream().map( obj ->{
                 Object o = hashOperations.get(obj.toString());
                 assert o != null;
                 return JSON.parseObject(o.toString(), CartItem.class);
             }
            ).collect(Collectors.toList());
        }
        return items;
    }

    private CartItem getCartItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOperations = getOptions();
        //全部要转成String
        Object item = hashOperations.get(skuId.toString());
        CartItem cartItem = new CartItem();
        if (item != null) {
            cartItem = JSON.parseObject(item.toString(), CartItem.class);
        }
        return cartItem;
    }

}
