package com.atguigu.gulimall.cart.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewSkuPriceVo {
    private Long skuId;
    private BigDecimal price;
}
