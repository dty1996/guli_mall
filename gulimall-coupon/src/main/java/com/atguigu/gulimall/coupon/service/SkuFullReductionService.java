package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.to.SkuReduceTo;
import com.atguigu.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 15:20:19
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveInfo(SkuReduceTo skuReduceTo);
}

