package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
