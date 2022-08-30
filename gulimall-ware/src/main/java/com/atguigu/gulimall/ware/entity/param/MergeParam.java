package com.atguigu.gulimall.ware.entity.param;


import lombok.Data;

import java.util.List;

@Data
public class MergeParam {

    /**
     * 采购单id
     */
    private Long purchaseId;

    /**
     * 合并项id集合
     */
    private List<Long> items;
}
