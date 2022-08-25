package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.params.AttrAttrgroupRelationParam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void removeByAttrIds(List<Long> asList);

    void removeParams(List<AttrAttrgroupRelationParam> params);
}

