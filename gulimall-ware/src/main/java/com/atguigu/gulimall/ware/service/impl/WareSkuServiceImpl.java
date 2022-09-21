package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.to.SkuStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.to.WareSkuLockTo;
import com.atguigu.gulimall.ware.entity.vo.OrderItemVo;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(wareId)) {
            Long wareInfoId = Long.parseLong(wareId);
            queryWrapper.eq(WareSkuEntity::getWareId, wareInfoId);
        }
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.like(WareSkuEntity::getSkuId, skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(PurchaseDetailEntity bId) {
        Long skuId = bId.getSkuId();
        Long wareId = bId.getWareId();
        Integer stock = bId.getSkuNum();
        WareSkuEntity wareSkuEntity = lambdaQuery()
                .eq(WareSkuEntity::getSkuId, skuId)
                .eq(WareSkuEntity::getWareId, wareId)
                .one();


        if (null == wareSkuEntity) {
            //远程调用查询skuName
            String skuName = "";
            try {
                R r = productFeignService.info(skuId);
                Map<String, Object> skuInfo =  (Map<String, Object>) r.get("skuInfo");
                skuName = (String) skuInfo.get("skuName");
            }catch (Exception e) {
                e.printStackTrace();
            }
            //新增库存
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setSkuName(skuName);
            wareSku.setStock(stock);
            wareSku.setStockLocked(0);
            save(wareSku);
        } else {
            baseMapper.updateStock(stock, wareSkuEntity.getId());
        }
    }

    @Override
    public List<SkuStockVo> queryStockBySku(List<Long> skuIds) {

        return skuIds.stream().map(skuId -> {
            SkuStockVo skuStockVo = new SkuStockVo();
            skuStockVo.setSkuId(skuId);
            Long stock = baseMapper.selectStockBySku(skuId);
            if (stock != null && stock > 0L) {
                skuStockVo.setHasStock(true);
            } else {
                skuStockVo.setHasStock(false);
            }
            return skuStockVo;
        }).collect(Collectors.toList());
    }


    /**
     * 锁定库存
     * @param wareSkuLockTo
     * @return
     */
    @Override
    public Boolean lockWare(WareSkuLockTo wareSkuLockTo) {
        ////TODO 保存库存工作单详情,方便追溯

        List<OrderItemVo> locks = wareSkuLockTo.getOrderItems();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //找出那些仓库有货
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //仓库为空
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                //返回1成功(1行受影响),返回0失败(0行受影响)
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    break;
                }
            }
            if (!skuStocked) {
                //当前商品所有仓库都无货(没锁住库存)
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    /**
     * 内部类
     */
    @Data
    static
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}
