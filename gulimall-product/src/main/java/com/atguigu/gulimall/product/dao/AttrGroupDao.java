package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 属性分组
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    Page<AttrGroupEntity> selectByCategoryId(IPage<AttrGroupEntity> page, @Param("categoryId") Long categoryId);
}
