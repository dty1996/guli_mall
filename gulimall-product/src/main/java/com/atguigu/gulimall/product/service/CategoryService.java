package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.vo.CategoryVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:50
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryVo> listWithTree();

    void removeBatchByIds(Long[] catIds);

    Long[] selectPathByCategotyId(Long catelogId);

    List<String> selectNameByCatelogId(Long catelogId);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, Object> getCatalogJson();
}

