package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.vo.BrandVo;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<BrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = null;
        if (params.containsKey(PmsConstant.KEY)) {

            JSONObject map = new JSONObject(params);
            key = map.getString(PmsConstant.KEY);
        }
        //模糊查询
        queryWrapper.like(StringUtils.isNotBlank(key),BrandEntity::getName, key);
        queryWrapper.orderByDesc(BrandEntity::getSort);
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );


        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(BrandEntity brand) {
        Long brandId = brand.getBrandId();
        BrandEntity one = lambdaQuery().eq(BrandEntity::getBrandId, brandId).one();
        if (brand.getName().equals(one.getName())) {
            updateById(brand);
        } else {
            //修改品牌关联表中的品牌名称
            LambdaQueryWrapper<CategoryBrandRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CategoryBrandRelationEntity::getBrandId, brand.getBrandId());
            List<CategoryBrandRelationEntity> list = categoryBrandRelationService.list(queryWrapper);
            list.forEach(per -> {
                per.setBrandName(brand.getName());
            });
            categoryBrandRelationService.updateBatchById(list);
            //修改品牌
            updateById(brand);
        }
    }

    /**
     * 根据三级分类查询品牌名
     * @param params
     * @return
     */
    @Override
    public List<BrandVo> queryBrandListByCatelogId(Map<String, Object> params) {
        JSONObject jsonObject = new JSONObject(params);
        Long catelogId = jsonObject.getObject("catId", Long.class);
        List<BrandVo> brandVos = baseMapper.selectBrandListByCatelogId(catelogId);
        return brandVos;
    }
}
