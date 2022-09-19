package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.vo.*;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.SkuImagesService;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private ThreadPoolExecutor executor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据spu查询skus
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return  lambdaQuery().eq(SkuInfoEntity::getSpuId, spuId).list();
    }

    /**
     * 查询商品详情
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVo item(Long skuId) {
        /**
         * 异步编排
         */
        SkuItemVo skuItemVo = new SkuItemVo();
        //sku基本信息
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture
                .supplyAsync(() -> {
                    SkuInfoEntity skuInfoEntity = lambdaQuery().eq(SkuInfoEntity::getSkuId, skuId).one();
                    skuItemVo.setInfo(skuInfoEntity);
                    return skuInfoEntity;
                }, executor);
        //sku图片信息
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.lambdaQuery().eq(SkuImagesEntity::getSkuId, skuId).list();
            skuItemVo.setImages(images);
        }, executor);

        //销售属性信息
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((sku) -> {
            List<SkuAttrVo> saleAttrVos = skuInfoDao.selectSaleAttrs(sku.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        //描述信息
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((sku) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.lambdaQuery().eq(SpuInfoDescEntity::getSpuId, sku.getSpuId()).one();
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> spuAttrFuture = infoFuture.thenAcceptAsync((sku) -> {
            List<SpuItemAttrGroupVo> spuAttrVos = attrGroupService.selectSpuAttrsByCatalogId(sku.getCatalogId(), sku.getSpuId());
            skuItemVo.setGroupAttrs(spuAttrVos);
        });

        //等待完成结果
        try {
            CompletableFuture.allOf(imageFuture, saleAttrFuture, descFuture, spuAttrFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return skuItemVo;
    }

    /**
     * 获取最新价格
     * @param skuIds skuId集合
     * @return List<NewSkuPriceVo>
     */
    @Override
    public List<NewSkuPriceVo> getNewSkuPrice(List<Long> skuIds) {
        List<SkuInfoEntity> list = lambdaQuery().in(SkuInfoEntity::getSkuId, skuIds).list();
        return list.stream().map(skuInfoEntity -> {
            NewSkuPriceVo newSkuPriceVo = new NewSkuPriceVo();
            newSkuPriceVo.setSkuId(skuInfoEntity.getSkuId());
            newSkuPriceVo.setPrice(skuInfoEntity.getPrice());
            return newSkuPriceVo;
        }).collect(Collectors.toList());

    }
}
