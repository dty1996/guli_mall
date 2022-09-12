package com.atguigu.gulimall.product.entity.vo;


import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.params.Attr;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    /**
     * sku基本信息
     */
    private SkuInfoEntity info;
    /**
     * sku图片信息
     */
    private List<SkuImagesEntity> images;

    private  boolean hasStock = true;

    /**
     * sku销售属性组合
     */
    private List<SkuAttrVo> saleAttr;

    /**
     * 获取spu的介绍
     */
    private SpuInfoDescEntity desp;

    /**
     * 获取spu的规格参数信息
     */
    private List<SpuItemAttrGroupVo> groupAttrs;
}
