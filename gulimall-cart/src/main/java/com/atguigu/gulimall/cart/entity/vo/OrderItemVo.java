package com.atguigu.gulimall.cart.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;
}
