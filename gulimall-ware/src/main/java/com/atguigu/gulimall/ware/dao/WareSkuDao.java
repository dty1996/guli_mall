package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:39:39
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateStock(@Param("stock") Integer stock, @Param("id") Long id);

    Long selectStockBySku(@Param("skuId") Long skuId);

    List<Long> listWareIdHasSkuStock(@Param("skuId")Long skuId);

    Long lockSkuStock(@Param("skuId")Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void releaseStock(Long skuId, Integer skuNum, Long wareId);
}
