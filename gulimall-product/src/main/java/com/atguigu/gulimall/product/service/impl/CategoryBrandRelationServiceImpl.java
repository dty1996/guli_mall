package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    private static final String branId = "brandId";

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Override
    public List<CategoryBrandRelationEntity> queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        Long brandId = null;
        if (params.containsKey(branId)) {
            JSONObject param =  new JSONObject(params);
            String brandStr = param.getString(branId);
            brandId = Long.parseLong(brandStr);
        }
        if (null != brandId) {
            queryWrapper.eq(CategoryBrandRelationEntity::getBrandId, brandId);
        }
        List<CategoryBrandRelationEntity> list = baseMapper.selectList(queryWrapper);


        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategoryBrandRelation(CategoryBrandRelationEntity categoryBrandRelation) {
        BrandEntity brand = brandService.lambdaQuery().eq(BrandEntity::getBrandId, categoryBrandRelation.getBrandId()).one();
        CategoryEntity category = categoryService.lambdaQuery().eq(CategoryEntity::getCatId, categoryBrandRelation.getCatelogId()).one();
        categoryBrandRelation.setBrandName(brand.getName());
        categoryBrandRelation.setCatelogName(category.getName());
        save(categoryBrandRelation);
    }
}