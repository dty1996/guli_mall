package com.atguigu.gulimall.order.entity.to;

import com.atguigu.gulimall.order.entity.vo.OrderItemVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class WareSkuLockTo implements Serializable {
    private String orderSn;
    private List<OrderItemVo> orderItems;
}
