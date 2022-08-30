package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 满减阶梯会员信息
 */
@Data
public class SkuReduceTo {
    private Long id;
    /**
     * spu_id
     */
    private Long skuId;
    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 折后价
     */
    private BigDecimal price;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer countStatus;

    private Integer priceStatus;

    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;


    /**
     * 会员信息
     */
    private List<MemberPriceTo> memberPriceToList;
}
