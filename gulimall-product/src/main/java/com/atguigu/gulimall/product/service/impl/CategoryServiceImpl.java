package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.entity.vo.Catalog2Vo;
import com.atguigu.gulimall.product.entity.vo.CategoryVo;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

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

    /**
     * 查询三级分类名称
     * @param catelogId
     * @return
     */
    @Override
    public List<String> selectNameByCatelogId(Long catelogId) {
        List<String> list = new ArrayList<>();
        List<String> pathList = findParentPathName(catelogId, list);
        Collections.reverse(pathList);
        return pathList;
    }

    /**
     * 递归查询三级分类名称
     * @param catelogId
     * @param list
     * @return
     */
    private List<String> findParentPathName(Long catelogId, List<String> list) {
        CategoryEntity byId = this.getById(catelogId);
        list.add(byId.getName());
        if (!byId.getParentCid().equals(0L)) {
            findParentPathName(byId.getParentCid(), list);
        }
        return list;
    }

    /**
     * 查询一级分类
     * @return
     */
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return lambdaQuery().eq(CategoryEntity::getCatLevel, PmsConstant.LEVEL_1).list();
    }

    /**
     * 查询二三级分类内容
     */
    @Override
    public Map<String, Object> getCatalogJson() {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        //查询出所有一级分类的id
        queryWrapper.select(CategoryEntity::getCatId).eq(CategoryEntity::getCatLevel, PmsConstant.LEVEL_1);
        List<CategoryEntity> catalog1Ids = categoryDao.selectList(queryWrapper);
        Map<String, Object> catalogMap = new HashMap<>();
        catalog1Ids.forEach( category -> {
            Long catId = category.getCatId();

            //查出一级分类下的所有二级分类
            List<CategoryEntity> catalog2List = lambdaQuery().eq(CategoryEntity::getParentCid, catId).list();

            List<Catalog2Vo> catalog2Vos = catalog2List.stream().map(catalog2 -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                Long cat2Id = catalog2.getCatId();
                //查出二级分类下的所有三级分类
                List<CategoryEntity> catalog3List = lambdaQuery().eq(CategoryEntity::getParentCid, cat2Id).list();
                List<Catalog2Vo.Catalog3Vo> catalog3Vos = catalog3List.stream().map(catalog3 -> {
                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                    catalog3Vo.setCatalog2Id(catalog2.getCatId().toString());
                    catalog3Vo.setId(catalog3.getCatId().toString());
                    catalog3Vo.setName(catalog3.getName());
                    return catalog3Vo;
                }).collect(Collectors.toList());

                catalog2Vo.setId(catalog2.getCatId().toString());
                catalog2Vo.setCatalog1Id(catId.toString());
                catalog2Vo.setName(catalog2.getName());
                catalog2Vo.setCatalog3List(catalog3Vos);
                return catalog2Vo;
            }).collect(Collectors.toList());
            catalogMap.put(catId.toString(), catalog2Vos);
        });
        return catalogMap;
    }
}
