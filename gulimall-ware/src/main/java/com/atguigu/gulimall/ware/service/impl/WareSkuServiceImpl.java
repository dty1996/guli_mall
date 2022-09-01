package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.to.SkuStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(wareId)) {
            Long wareInfoId = Long.parseLong(wareId);
            queryWrapper.eq(WareSkuEntity::getWareId, wareInfoId);
        }
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.like(WareSkuEntity::getSkuId, skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(PurchaseDetailEntity bId) {
        Long skuId = bId.getSkuId();
        Long wareId = bId.getWareId();
        Integer stock = bId.getSkuNum();
        WareSkuEntity wareSkuEntity = lambdaQuery()
                .eq(WareSkuEntity::getSkuId, skuId)
                .eq(WareSkuEntity::getWareId, wareId)
                .one();


        if (null == wareSkuEntity) {
            //远程调用查询skuName
            String skuName = "";
            try {
                R r = productFeignService.info(skuId);
                Map<String, Object> skuInfo =  (Map<String, Object>) r.get("skuInfo");
                skuName = (String) skuInfo.get("skuName");
            }catch (Exception e) {
                e.printStackTrace();
            }
            //新增库存
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setSkuName(skuName);
            wareSku.setStock(stock);
            wareSku.setStockLocked(0);
            save(wareSku);
        } else {
            baseMapper.updateStock(stock, wareSkuEntity.getId());
        }
    }

    @Override
    public List<SkuStockVo> queryStockBySku(List<Long> skuIds) {

        List<SkuStockVo> skuStockVos = skuIds.stream().map(skuId -> {
            SkuStockVo skuStockVo = new SkuStockVo();
            skuStockVo.setSkuId(skuId);
            Long stock = baseMapper.selectStockBySku(skuId);
            if (stock != null && stock > 0L) {
                skuStockVo.setHasStock(true);
            } else {
                skuStockVo.setHasStock(false);
            }
            return skuStockVo;
        }).collect(Collectors.toList());

        return skuStockVos;
    }
}
