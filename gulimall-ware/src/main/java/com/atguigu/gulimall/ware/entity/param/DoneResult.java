package com.atguigu.gulimall.ware.entity.param;

import lombok.Data;

@Data
public class DoneResult {
    /**
     * 采购需求id
     */
    private Long itemId;

    /**
     * 采购需求状态
     */
    private Integer status;

    /**
     * 结果，失败需写出原因
     */
    private String result;
}
