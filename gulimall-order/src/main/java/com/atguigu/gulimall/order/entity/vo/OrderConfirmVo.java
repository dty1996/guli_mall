package com.atguigu.gulimall.order.entity.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */

public class OrderConfirmVo {

    @Getter @Setter
    private List<MemberAddressVo> address;

    @Setter @Getter
    private List<OrderItemVo> items;

    //发票信息... 优惠券...

    /**
     * 积分
     */
    @Getter @Setter
    private Integer integration;

    /**
     * 商品数量
     */
    private Integer count;

    /**
     * 订单token，防刷
     */
    @Getter @Setter
    private String token;

    @Getter @Setter
    private Map<Long,Boolean> stocks;

    /**
     * 订单总额
     */
    private BigDecimal total;


    /**
     * 应付总额
     */
    private BigDecimal payPrice;


    public Integer getCount() {
        Integer count = 0;
        if (this.items != null && this.items.size() > 0) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 获取订单总额
     * @return BigDecimal
     */
    public BigDecimal getTotal() {
        BigDecimal bigDecimal = new BigDecimal("0");
        if (this.items != null && this.items.size() > 0) {
            for (OrderItemVo itemVo : this.items) {
                BigDecimal itemTotalPrice = itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount()));
                bigDecimal = bigDecimal.add(itemTotalPrice);
            }
        }
        return bigDecimal;
    }

    /**
     * 获取应付总额 此处没有减去优惠券等信息 后续可以加入优惠券系统
     * @return BigDecimal
     */
     public BigDecimal getPayPrice() {
         return this.getTotal();
     }



}
