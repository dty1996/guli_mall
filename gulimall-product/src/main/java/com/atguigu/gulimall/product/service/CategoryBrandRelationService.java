package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:50
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    List<CategoryBrandRelationEntity> queryPage(Map<String, Object> params);

    void saveCategoryBrandRelation(CategoryBrandRelationEntity categoryBrandRelation);
}

