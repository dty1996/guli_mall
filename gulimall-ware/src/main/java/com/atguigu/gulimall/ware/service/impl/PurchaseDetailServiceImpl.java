package com.atguigu.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            Long wareIdLong = Long.parseLong(wareId);
            queryWrapper.eq(PurchaseDetailEntity::getWareId,wareIdLong);
        }

        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            Integer statusInt = Integer.parseInt(status);
            queryWrapper.eq(PurchaseDetailEntity::getStatus, statusInt);
        }

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and( w -> {
                w.like(PurchaseDetailEntity::getId, key).or().like(PurchaseDetailEntity::getSkuId, key);
            });
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> queryDetailsByPurchaseId(Long purchaseId) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetailEntity::getPurchaseId, purchaseId);
        return baseMapper.selectList(queryWrapper);
    }
}
