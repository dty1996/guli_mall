package com.atguigu.gulimall.order.entity.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class SubmitOrderResponseVo {

    /**
     * 订单信息
     */
    private OrderEntity order;

    /**
     * 结果码，code为0通过，其余为通过
     */
    private Integer code;
}
