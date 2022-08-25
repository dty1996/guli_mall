package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.vo.AttrVo;
import com.atguigu.gulimall.product.enums.AttrTypeEnum;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.CategoryService;
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

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupService attrGroupService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttr(AttrEntity attr) {

        baseMapper.insert(attr);
        //规格属性时有分组
        if (AttrTypeEnum.BASE.getCode().equals(attr.getAttrType()) && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrGroupId(attr.getAttrGroupId());
            entity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationService.save(entity);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAttr(AttrEntity attr) {
        //规格属性时有分组
        if (AttrTypeEnum.BASE.getCode().equals(attr.getAttrType())) {
            AttrAttrgroupRelationEntity one = attrAttrgroupRelationService.lambdaQuery().eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()).one();
            //新增属性分组
           if (one == null) {
                AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
                entity.setAttrId(attr.getAttrId());
                entity.setAttrGroupId(attr.getAttrGroupId());
                attrAttrgroupRelationService.saveOrUpdate(entity);
           } else {
               //修改属性分组
               if (!one.getAttrGroupId().equals(attr.getAttrGroupId())) {
                   one.setAttrGroupId(attr.getAttrGroupId());
                   attrAttrgroupRelationService.updateById(one);
               }
           }

        }

        updateById(attr);

    }


    @Override
    public AttrVo selectAttrVoInfo(Long attrId) {
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        AttrVo attrVo = new AttrVo();
        BeanUtils.copyProperties(attrEntity, attrVo);
        //查询三级分类路径
        Long[] path = categoryService.selectPathByCategotyId(attrEntity.getCatelogId());
        attrVo.setCatelogPath(path);
        AttrAttrgroupRelationEntity byId = attrAttrgroupRelationService.lambdaQuery().eq(AttrAttrgroupRelationEntity::getAttrId, attrId).one();
        if (null != byId) {
            Long attrGroupId = byId.getAttrGroupId();
            attrVo.setAttrGroupId(attrGroupId);
        }
        return attrVo;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<>()
        );
        IPage<AttrEntity> attrPage ;
        if (categoryId.equals(0L)) {
            attrPage  = attrDao.selectAttrsByCategoryId(page, null);
        } else {
            attrPage = attrDao.selectAttrsByCategoryId(page, categoryId);
        }


        return new PageUtils(attrPage);
    }

    /**
     * 查询所有分组
     * @param params
     * @return
     */
    @Override
    public PageUtils queryAttrNoAttrgroup(Map<String, Object> params, Long attrgroupId) {
        //查询分组的分类
        AttrGroupEntity attrGroup = attrGroupService.lambdaQuery().eq(AttrGroupEntity::getAttrGroupId, attrgroupId).one();
        Long catelogId = attrGroup.getCatelogId();
        JSONObject jsonObject = new JSONObject(params);
        Integer page1 = jsonObject.getObject("page", Integer.class);
        Integer limit = jsonObject.getObject("limit", Integer.class);
        Page<AttrEntity> page = new Page<>(page1, limit);
        String key = null;
        if (params.containsKey(PmsConstant.KEY)) {
            key = (String) params.get(PmsConstant.KEY);
        }
        Integer attrType = AttrTypeEnum.BASE.getCode();
        Page<AttrEntity> attrPage = baseMapper.findAttrNoAttrfoup(page, catelogId ,  attrType, key);
        return new PageUtils(attrPage);
    }

    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, Long categoryId, Integer attrType) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                new QueryWrapper<>());
        IPage<AttrEntity> attrVoIPage;
        String key = null;
        if (params.containsKey(PmsConstant.KEY)){
            key = (String) params.get(PmsConstant.KEY);
        }
        if (categoryId.equals(0L)) {
           attrVoIPage = baseMapper.selectAttrVoPage(page, null, attrType, key);
        } else {
            attrVoIPage = baseMapper.selectAttrVoPage(page, categoryId, attrType, key);
        }

        List<AttrEntity> records = attrVoIPage.getRecords();
        List<AttrVo> attrVos = new ArrayList<>();
        records.stream().forEach(per -> {
            AttrVo attrVo = new AttrVo();
            BeanUtils.copyProperties(per, attrVo);
            ////TODO 后续将三级分类放入缓存中，从缓存中取数据
            Long catelogId = per.getCatelogId();
            //根据目录id查询三级分类名称
            List<String> catelogNames = categoryService.selectNameByCatelogId(catelogId);
            String catelogName = "";
            for (String name : catelogNames) {
                catelogName += name + "/";
            }
            catelogName= catelogName.substring(0,catelogName.length() - 1);
            attrVo.setCatelogName(catelogName);
            attrVos.add(attrVo);
        });
        Page<AttrVo> attrVoPage = new Page<>();
        attrVoPage.setRecords(attrVos);
        attrVoPage.setTotal(attrVoIPage.getTotal());
        attrVoPage.setSize(attrVoIPage.getSize());
        attrVoPage.setCurrent(attrVoIPage.getCurrent());
        attrVoPage.setPages(attrVoIPage.getPages());
        return new PageUtils(attrVoPage);
    }

    /**
     * 批量删除属性
     * @param asList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAttr(List<Long> asList) {
        //删除属性
        attrDao.deleteBatchIds(asList);
        //删除属性分组关联
        attrAttrgroupRelationService.removeByAttrIds(asList);
    }

    private LambdaQueryWrapper<AttrEntity> extracted(Map<String, Object> params) {
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (params.containsKey(PmsConstant.KEY)) {
            String key = (String) params.get(PmsConstant.KEY);
            queryWrapper.like(AttrEntity::getAttrName, key).or().like(AttrEntity::getAttrId,key);
        }
        return queryWrapper;
    }
}
