package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    PageUtils queryAttrNoAttrgroup(Map<String, Object> params, Long attrgroupId);

    PageUtils queryAttrPage(Map<String, Object> params, Long categoryId, Integer attrType);

    void saveAttr(AttrEntity attr);

    void updateAttr(AttrEntity attr);

    AttrVo selectAttrVoInfo(Long attrId);

    void removeAttr(List<Long> asList);
}

