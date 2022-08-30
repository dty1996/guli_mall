package com.atguigu.gulimall.product.entity.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class Sku {
    private List<Attr> attr;
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private Integer fullCount;
    private BigDecimal discount;
    private Integer countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer priceStatus;
    private List<MemberPrice> memberPrice;
}
