package com.atguigu.gulimall.ware.entity.to;

import com.atguigu.gulimall.ware.entity.vo.OrderItemVo;
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
