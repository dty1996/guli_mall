package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.Vo.AttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品属性
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    IPage<AttrEntity> selectAttrsByCategoryId(IPage<AttrEntity> page, @Param("categoryId") Long categoryId);

    IPage<AttrEntity> selectAttrVoPage( IPage<AttrEntity> page, @Param("catelogId") Long categoryId , @Param("attrType") Integer attrType, @Param("key") String key);
}
