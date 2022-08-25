package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.vo.BrandVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Mapper
public interface BrandDao extends BaseMapper<BrandEntity> {

    List<BrandVo> selectBrandListByCatelogId(@Param("catelogId") Long catelogId);
}
