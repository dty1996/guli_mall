package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.Constant;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.vo.AttrgroupWithAttrsVo;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private AttrAttrgroupRelationService  attrAttrgroupRelationService;



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

    /**
     * 根据三级分类id求属性分组及分组下的属性值
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrgroupWithAttrsVo> queryAttrgroupWithAttrByCatelogId(Long catelogId) {
        //查找分类下的属性分组
        List<AttrGroupEntity> list = lambdaQuery().eq(AttrGroupEntity::getCatelogId, catelogId).list();
        List<AttrgroupWithAttrsVo> attrgroupWithAttrsVos = list.stream().map(group -> {
            AttrgroupWithAttrsVo attrgroupWithAttrsVo = new AttrgroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrgroupWithAttrsVo);
            //查询分组下的属性值
            List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService.lambdaQuery().eq(AttrAttrgroupRelationEntity::getAttrGroupId, group.getAttrGroupId()).list();
            ArrayList<AttrEntity> attrs = new ArrayList<>();
            relationEntities.stream().forEach(relation -> {
                Long attrId = relation.getAttrId();
                AttrEntity byId = attrService.getById(attrId);
                attrs.add(byId);
            });
            attrgroupWithAttrsVo.setAttrs(attrs);
            return attrgroupWithAttrsVo;
        }).collect(Collectors.toList());
        return attrgroupWithAttrsVos;
    }
}
