package com.atguigu.gulimall.product.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
