package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> queryBySpu(Long spuId) {
         return lambdaQuery().eq(ProductAttrValueEntity::getSpuId, spuId).list();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBySpuId(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities) {
        productAttrValueEntities.forEach(per -> {
            lambdaUpdate().eq(ProductAttrValueEntity::getAttrId, per.getAttrId())
                    .eq(ProductAttrValueEntity::getSpuId, spuId)
                    .update(per);
        });
    }
}
