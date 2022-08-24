package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.Constant;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrService attrService;



    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        IPage<AttrGroupEntity> attrGroupEntityPage = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>());
        IPage<AttrGroupEntity> attrGroup ;
        String key = null;
        if (params.containsKey(PmsConstant.KEY)) {
            JSONObject map = new JSONObject(params);
            key = map.getString(PmsConstant.KEY);
        }
        if (categoryId == 0) {
           attrGroup = attrGroupDao.selectAllPage(attrGroupEntityPage, key);
        } else {

            attrGroup  = attrGroupDao.selectByCategoryId(attrGroupEntityPage, categoryId, key);
        }


        return new PageUtils(attrGroup);
    }


    @Override
    public List<AttrEntity> selectAttrByAttrgroupId(Map<String, Object> params, Long attrgroupId) {
        return baseMapper.queryAttrByAttrgroupId(attrgroupId);
    }


}
