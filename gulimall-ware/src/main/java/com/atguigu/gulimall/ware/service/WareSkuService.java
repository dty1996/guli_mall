package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:39:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(PurchaseDetailEntity bId);
}

