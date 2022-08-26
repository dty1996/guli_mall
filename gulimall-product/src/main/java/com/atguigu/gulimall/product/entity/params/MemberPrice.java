package com.atguigu.gulimall.product.entity.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
public class MemberPrice {
    private Long id;
    private String name;
    private BigDecimal price;
}


