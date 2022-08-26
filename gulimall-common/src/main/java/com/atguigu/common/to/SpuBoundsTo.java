package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
public class SpuBoundsTo {
    /**
     *spu_id
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
}
