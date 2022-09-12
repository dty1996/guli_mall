package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.vo.AttrgroupWithAttrsVo;
import com.atguigu.gulimall.product.entity.vo.SpuAttrVo;
import com.atguigu.gulimall.product.entity.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    List<AttrEntity> selectAttrByAttrgroupId(Map<String, Object> params, Long attrgroupId);

    List<AttrgroupWithAttrsVo> queryAttrgroupWithAttrByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> selectSpuAttrsByCatalogId(Long catalogId, Long spuId);
}

