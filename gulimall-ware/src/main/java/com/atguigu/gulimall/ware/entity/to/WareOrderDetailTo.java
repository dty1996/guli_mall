package com.atguigu.gulimall.ware.entity.to;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author dty
 * @date 2022/9/23 10:20
 */
@Data
public class WareOrderDetailTo implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;


    private Long wareId;

    private Integer status;
}
