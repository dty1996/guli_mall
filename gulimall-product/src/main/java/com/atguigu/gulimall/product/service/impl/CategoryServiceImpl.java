package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.entity.vo.CategoryVo;
import com.atguigu.gulimall.product.entity.vo.Catelog2Vo;
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


    @Override
    public List<CategoryEntity> getLevel1() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        /**
         * 优化:将数据库中的多次查询变为一次,存至缓存selectList,需要的数据从list取出,避免频繁的数据库交互
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有1级分类
        List<CategoryEntity> level1 = getParent_cid(selectList, 0L);
        //2.封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    //1.查出1级分类中所有2级分类
                    List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
                    //2.封装上面的结果
                    List<Catelog2Vo> catelog2Vos = null;
                    if (categoryEntities != null) {
                        catelog2Vos = categoryEntities.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                            //查询当前2级分类的3级分类
                            List<CategoryEntity> level3 = getParent_cid(selectList, l2.getCatId());
                            if (level3 != null) {
                                List<Catelog2Vo.Catelog3Vo> collect = level3.stream().map(l3 -> {
                                    //封装指定格式
                                    Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                    return catelog3Vo;
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(collect);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }
        ));
        return parent_cid;
    }
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

}
