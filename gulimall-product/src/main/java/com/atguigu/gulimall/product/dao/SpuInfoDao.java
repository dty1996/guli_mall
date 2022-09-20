package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.entity.to.SpuInfoWithSkuIdTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu信息
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updatePublishStatus(@Param("spuId") Long spuId, @Param("code") Integer code);

    List<SpuInfoWithSkuIdTo> selectSpuInfosBySkuIds(@Param("skuIds") List<Long> skuIds);
}
