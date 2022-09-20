package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.LoginUserVo;
import com.atguigu.common.to.SkuStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.constants.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.to.SpuInfoEntity;
import com.atguigu.gulimall.order.entity.vo.*;
import com.atguigu.gulimall.order.enums.ConfirmStatusEnum;
import com.atguigu.gulimall.order.enums.StatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.thread.OrderThreadLocal;
import com.atguigu.gulimall.order.thread.UserThreadLocal;
import com.atguigu.gulimall.order.utils.RedisLuaUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.util.StringUtils;
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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedisLuaUtil redisLuaUtil;

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
        orderConfirmVo.setIntegration(loginUserVo.getIntegration());
        //设置订单token,防止重复提交订单
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setToken(token);
        String tokenKey = OrderConstant.ORDER_TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(tokenKey, token, OrderConstant.ORDER_TOKEN_EXPIRE, TimeUnit.SECONDS);
        return orderConfirmVo;
    }


    /**
     * 提交订单
     * @param submitOrderVo
     * @return
     */
    @Override
    public SubmitOrderResponseVo submitOrder(SubmitOrderVo submitOrderVo) {

        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        //令牌token，防止重复提交
        LoginUserVo loginUserVo = UserThreadLocal.get();
        String tokenKey = OrderConstant.ORDER_TOKEN_PREFIX + loginUserVo.getId();
        String token = submitOrderVo.getOrderToken();
        //令牌的对比和删除必须保证原子性
        Long compareAndDelete = redisLuaUtil.compareAndDelete(tokenKey, token);
        if (!OrderConstant.SUCCESS_CAD.equals(compareAndDelete)) {
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }
        //校验令牌成功 执行下面流程
        String orderSn = IdWorker.getTimeId();
        //创建订单实体类
        OrderEntity orderEntity = buildOrderEntity(orderSn);


        //创建订单项
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        return null;
    }


    /**
     * 创建
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> orderItems = cartFeignService.getOrderItem();
        List<Long> skuIds = orderItems.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        //通过skuIds获得spu信息
        Map<Long, SpuInfoEntity> map = productFeignService.getSpuInfosBySkuIds(skuIds);


        return orderItems.stream().map(item -> {
            OrderItemEntity orderItemEntity = buildOrderItem(item);
            //设置spu信息
            SpuInfoEntity spuInfoData = map.get(item.getSkuId());
            orderItemEntity.setSpuId(spuInfoData.getId());
            orderItemEntity.setSpuName(spuInfoData.getSpuName());
            orderItemEntity.setCategoryId(spuInfoData.getCatalogId());
            //设置订单号
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());

    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();


        //商品的sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);


        return orderItemEntity;

    }


    /**
     * 创建订单实体类
     * @param orderSn 订单号
     * @return OrderEntity
     */
    private OrderEntity buildOrderEntity(String orderSn) {
        SubmitOrderVo submitOrderVo = OrderThreadLocal.get();
        LoginUserVo loginUserVo = UserThreadLocal.get();
        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        //获取地址信息
        R fare = wareFeignService.getFareByAddress(submitOrderVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费
        order.setFreightAmount(fareVo.getFare());
        //设置地址信息
        MemberAddressVo address = fareVo.getAddress();
        order.setBillReceiverPhone(address.getPhone());
        order.setReceiverName(address.getName());
        order.setBillReceiverEmail(loginUserVo.getEmail());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setNote(submitOrderVo.getNote());

        //设置订单状态
        order.setStatus(StatusEnum.TO_SEND.getCode());
        order.setConfirmStatus(ConfirmStatusEnum.NOT_CHECK.getCode());
        order.setAutoConfirmDay(OrderConstant.DEFAULT_CONFIRM_DAY);
        return order;
    }
}
