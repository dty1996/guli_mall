package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.params.SpuAddParam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存商品信息
     * @param spuAddParam
     */
    void saveSpu(SpuAddParam spuAddParam);
}

