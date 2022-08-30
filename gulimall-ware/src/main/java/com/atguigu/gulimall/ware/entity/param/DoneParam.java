package com.atguigu.gulimall.ware.entity.param;

import lombok.Data;

import java.util.List;

@Data
public class DoneParam {
    /**
     * 采购单id
     */
    private Long id;

    /**
     * 采购需求集合
     */
    private List<DoneResult> items;
}
