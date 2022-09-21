package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.LoginUserVo;
import com.atguigu.common.to.SkuStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.constants.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.to.SpuInfoEntity;
import com.atguigu.gulimall.order.entity.to.WareSkuLockTo;
import com.atguigu.gulimall.order.entity.vo.*;
import com.atguigu.gulimall.order.enums.ConfirmStatusEnum;
import com.atguigu.gulimall.order.enums.StatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.thread.OrderThreadLocal;
import com.atguigu.gulimall.order.thread.UserThreadLocal;
import com.atguigu.gulimall.order.utils.RedisLuaUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * @author Administrator
 */
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

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
                new QueryWrapper<>()
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
     * 提交订单  涉及到分布式事务问题
     *  在分布式系统中本地事务无法对其他系统的事务进行操作
     *  事务是用代理对象来控制的，如果在 a 里面调用的是同一个 service 的 b、c方法，相当于把 b、c 的代码复制、粘贴过来了
     *  直接调用的本类的方法，而不是通过AOP代理增强调用，也就是跳过了代理
     *  网络问题
     * @param submitOrderVo 提交vo
     * @return SubmitOrderResponseVo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitOrderResponseVo submitOrder(SubmitOrderVo submitOrderVo) {
        OrderThreadLocal.set(submitOrderVo);
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

        // 设置价格
        computePrice(orderEntity, orderItems);


        //对比价格
        BigDecimal payPrice = submitOrderVo.getPayPrice();
        BigDecimal orderPrice = orderEntity.getPayAmount();
        //价格相差0.01内可以认为符合
        BigDecimal subtract = payPrice.subtract(orderPrice);

        if (Math.abs(subtract.doubleValue()) >= OrderConstant.DEVIATION_PRICE) {
            submitOrderResponseVo.setCode(2);
            return submitOrderResponseVo;
        }
        //下订单,保存订单项
        orderDao.insert(orderEntity);
        orderItemService.saveBatch(orderItems);
        //验价通过执行锁定库存操作
        List<OrderItemVo> orderItemVos = orderItems.stream().map(item -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(item.getSkuId());
            orderItemVo.setCount(item.getSkuQuantity());
            return orderItemVo;
        }).collect(Collectors.toList());
        WareSkuLockTo wareSkuLockTo = new WareSkuLockTo();
        wareSkuLockTo.setOrderItems(orderItemVos);
        wareSkuLockTo.setOrderSn(orderSn);
        R ware = wareFeignService.lockWare(wareSkuLockTo);
        if (ware.getCode() == 0) {
            //锁定成功
            submitOrderResponseVo.setCode(0);
            submitOrderResponseVo.setOrder(orderEntity);
        } else {
            submitOrderResponseVo.setCode(3);
        }
        return submitOrderResponseVo;

    }


    /**
     * 创建订单项
     * @param orderSn 订单号
     * @return List<OrderItemEntity>
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> orderItems = cartFeignService.getOrderItem();
        List<Long> skuIds = orderItems.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        //通过skuIds获得spu信息
        R spuInfosBySkuIds = productFeignService.getSpuInfosBySkuIds(skuIds);
        Map<Long, SpuInfoEntity> map = spuInfosBySkuIds.getData(new TypeReference<Map<Long, SpuInfoEntity>>() {});

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
        orderItemEntity.setSkuQuantity(item.getCount());
        //4.优惠信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        //5.积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().intValue() * item.getCount());
        orderItemEntity.setGiftIntegration(item.getPrice().intValue() * item.getCount());
        //6.订单项价格信息
        //实际金额
        BigDecimal orgin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orgin.subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

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

    /**
     * 设置价格
     *
     * @param orderEntity 订单项
     * @param itemEntities 购物项
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        //订单总额,计算各种优惠的总额,计算积分和成长值总额
        BigDecimal couponAmount = new BigDecimal("0.0");
        BigDecimal promotionAmount = new BigDecimal("0.0");
        BigDecimal integrationAmount = new BigDecimal("0.0");
        Integer integration = 0;
        Integer growth = 0;
        BigDecimal total = new BigDecimal("0.0");
        for (OrderItemEntity entity : itemEntities) {
            couponAmount = couponAmount.add(entity.getCouponAmount());
            promotionAmount = promotionAmount.add(entity.getPromotionAmount());
            integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
            growth += entity.getGiftGrowth();
            integration += entity.getGiftIntegration();
            total = total.add(entity.getRealAmount());
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setGrowth(growth);
        orderEntity.setIntegration(integration);
    }


}
