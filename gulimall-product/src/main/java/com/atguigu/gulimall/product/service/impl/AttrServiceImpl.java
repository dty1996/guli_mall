package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.Constant;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.entity.Vo.AttrVo;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private CategoryService categoryService;

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
    public PageUtils queryAttrNoAttrgroup(Map<String, Object> params) {

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                extracted(params));
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, Long categoryId, Integer attrType) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                new QueryWrapper<>());
        IPage<AttrEntity> attrVoIPage;
        if (categoryId.equals(0L)) {
           attrVoIPage = baseMapper.selectAttrVoPage(page, null, attrType);
        } else {
            attrVoIPage = baseMapper.selectAttrVoPage(page, categoryId, attrType);
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
                //不是最后一个
                if (!name.equals(catelogNames.get(catelogNames.size() -1))) {
                    catelogName += name + "/";
                }
            }
        });
        return null;
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
