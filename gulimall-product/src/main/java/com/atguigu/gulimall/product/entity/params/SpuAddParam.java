package com.atguigu.gulimall.product.entity.params;

import com.atguigu.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class SpuAddParam {
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private Integer publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttr> baseAttrs;
    private List<Sku> skus;
}
