package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.entity.params.*;
import com.atguigu.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDao spuInfoDao;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ProductAttrValueService productAttrValueService;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品信息
     * @param spuAddParam
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void saveSpu(SpuAddParam spuAddParam) {

        //保存spuInfo信息
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(spuAddParam, spuInfo);
        save(spuInfo);

        //保存spuInfo-desc
        List<String> decriptList = spuAddParam.getDecript();
        List<SpuInfoDescEntity> spuInfoDescList = decriptList.stream().map(per -> {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuInfo.getId());
            spuInfoDescEntity.setDecript(per);
            return  spuInfoDescEntity;
        }).collect(Collectors.toList());
        spuInfoDescService.saveBatch(spuInfoDescList);

        //保存spuInfo-image
        List<String> images = spuAddParam.getImages();
        List<SpuImagesEntity> imageArrayList =
        images.stream().map( per -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfo.getId());
            spuImagesEntity.setImgUrl(per);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(imageArrayList);

        //保存商品属性值 spu-attr-value
        List<BaseAttr> baseAttrs = spuAddParam.getBaseAttrs();
        List<ProductAttrValueEntity> attrValueList  =  baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValue = new ProductAttrValueEntity();
            BeanUtils.copyProperties(baseAttr, productAttrValue);
            productAttrValue.setSpuId(spuInfo.getId());
           return productAttrValue;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(attrValueList);

        //保存商品积分信息
        Bounds bounds = spuAddParam.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfo.getId());
            //open-feign调用

        //保存sku信息
        List<Sku> skus = spuAddParam.getSkus();
        skus.forEach(sku -> {

            //保存skuInfo信息
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
              //初始化skuInfo信息
            skuInfoEntity.setSpuId(spuInfo.getId());
            skuInfoEntity.setBrandId(spuAddParam.getBrandId());
            skuInfoEntity.setCatalogId(spuAddParam.getCatalogId());
            skuInfoEntity.setSaleCount(PmsConstant.INIT_COUNT);
            skuInfoService.save(skuInfoEntity);

            //保存skuImage信息
            List<Images> skuImages = sku.getImages();
            List<SkuImagesEntity> skuImagesList =
            skuImages.stream().map(skuImage -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                BeanUtils.copyProperties(skuImage, skuImagesEntity);
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuImagesEntity;
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesList);

            //保存属性值信息
            List<Attr> skuAttrs = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueList =
            skuAttrs.stream().map(skuAttr -> {
                SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(skuAttr, skuSaleAttrValue);
                skuSaleAttrValue.setSkuId(skuInfoEntity.getSkuId());
                return skuSaleAttrValue;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
        });

    }
}
