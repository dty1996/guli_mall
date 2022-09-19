package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.to.LoginUserVo;
import com.atguigu.common.to.SkuStockVo;
import com.atguigu.gulimall.order.entity.vo.MemberAddressVo;
import com.atguigu.gulimall.order.entity.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.entity.vo.OrderItemVo;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.thread.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {


        //从ThreadLocal中取出用户id
        LoginUserVo loginUserVo = UserThreadLocal.get();
        Long userId = loginUserVo.getId();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //查询用户地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressVos = memberFeignService.getMemberAddress(userId);
            orderConfirmVo.setAddress(addressVos);
        }, executor);

        //获取订单中商品数据（从购物车中获取）

        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            //openFeign远程调用时没有请求头
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItems = cartFeignService.getOrderItem();
            orderConfirmVo.setItems(orderItems);

        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuStockVo> skuStockVos = wareFeignService.queryStockBySku(collect);
            if (skuStockVos != null ) {
                Map<Long, Boolean> map = skuStockVos.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(map);
            }
        }, executor);

        CompletableFuture.allOf(addressFuture, orderItemFuture).get();
        System.out.println(orderConfirmVo);
        orderConfirmVo.setIntegration(loginUserVo.getIntegration());
        return orderConfirmVo;
    }
}
