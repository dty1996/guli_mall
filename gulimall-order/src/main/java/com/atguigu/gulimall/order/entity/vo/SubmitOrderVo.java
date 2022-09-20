package com.atguigu.gulimall.order.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
public class SubmitOrderVo {
    /**
     * 地址id
     */
    private Long addrId;
    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 支付令牌，防止重复提交订单
     */
    private String orderToken;

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    /**
     * 备注信息
     */
    private String note;
}
