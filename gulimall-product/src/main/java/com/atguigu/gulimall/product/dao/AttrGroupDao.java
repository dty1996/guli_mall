package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.vo.SpuAttrVo;
import com.atguigu.gulimall.product.entity.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    Page<AttrGroupEntity> selectByCategoryId(IPage<AttrGroupEntity> page, @Param("categoryId") Long categoryId, @Param("key") String key);

    IPage<AttrGroupEntity> selectAllPage(IPage<AttrGroupEntity> attrGroupEntityPage,@Param("key") String key);

    List<AttrEntity> queryAttrByAttrgroupId(@Param("attrgroupId") Long attrgroupId);

    List<SpuItemAttrGroupVo> selectSpuAttrsByCatalogId(@Param("catalogId") Long catalogId, @Param("spuId") Long spuId);
}
