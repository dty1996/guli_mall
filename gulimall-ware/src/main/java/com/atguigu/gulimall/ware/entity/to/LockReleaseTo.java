package com.atguigu.gulimall.ware.entity.to;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author dty
 * @date 2022/9/23 9:30
 * 订单解锁TO
 */
@Data
public class LockReleaseTo  implements Serializable {

    private Long  taskId;

    private WareOrderDetailTo  wareOrderDetailTo;

}
