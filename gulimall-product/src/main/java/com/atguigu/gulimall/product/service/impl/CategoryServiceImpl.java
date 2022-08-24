package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.entity.Vo.CategoryVo;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询树形结构
     * @return
     */
    @Override
    public  List<CategoryVo> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryVo> categoryVos = new ArrayList<>();
        categoryEntities.forEach(per -> {
            CategoryVo categoryVo = new CategoryVo();
            BeanUtils.copyProperties(per,categoryVo);
            categoryVos.add(categoryVo);
        });
        //顶层
        List<CategoryVo> baseCategory = categoryVos.stream()
                .filter(per -> per.getParentCid().equals(0L))
                .peek(categoryEntity -> {
                    //获取子节点
                    categoryEntity.setChildren(getChildren(categoryEntity, categoryVos));
                })
                .sorted(Comparator.comparingInt(per -> (per.getSort() == null ? 0 : per.getSort())))
                .collect(Collectors.toList());

        return baseCategory;
    }

    /**
     * 递归查询子种类
     * @param categoryEntity
     * @param categoryVos
     * @return
     */
    private List<CategoryVo> getChildren(CategoryVo categoryEntity, List<CategoryVo> categoryVos) {
        return categoryVos.stream()
                .filter(per -> per.getParentCid().equals(categoryEntity.getCatId()))
                .peek(per -> {
                    //递归查询子节点
                    per.setChildren(getChildren(per,categoryVos));
                })
                .sorted(Comparator.comparingInt(per -> (per.getSort() == null ? 0 : per.getSort())))
                .collect(Collectors.toList());
    }

    /**
     * 逻辑删除商品种类
     * @param catIds
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatchByIds(Long[] catIds) {
        ////TODO 刪除前判断是否商品种类这是否有关联

        baseMapper.deleteBatchIds(Arrays.asList(catIds));
    }


    /**
     * 查询当前的路径
     * @param catelogId
     * @return
     */
    @Override
    public Long[] selectPathByCategotyId(Long catelogId) {
        List<Long> list = new ArrayList<>();
        List<Long> path = findParentPath(catelogId, list);
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);

        //根据当前分类id查询信息
        CategoryEntity byId = this.getById(catelogId);
        //如果当前不是父分类
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }

}
