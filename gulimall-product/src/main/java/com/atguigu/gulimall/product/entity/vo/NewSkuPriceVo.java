package com.atguigu.gulimall.product.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class NewSkuPriceVo implements Serializable {

    private Long skuId;

    private BigDecimal price;
}
