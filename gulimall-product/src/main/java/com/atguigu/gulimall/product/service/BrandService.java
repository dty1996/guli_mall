package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.vo.BrandVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateBrand(BrandEntity brand);

    List<BrandVo> queryBrandListByCatelogId(Map<String, Object> params);
}

