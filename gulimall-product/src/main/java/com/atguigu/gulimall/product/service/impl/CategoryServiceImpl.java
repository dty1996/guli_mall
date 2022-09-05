package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

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
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>>  catalogMap ;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(PmsConstant.CATALOG_JSON))) {
            System.out.println("从缓存中取数据");
            String catalogJsonStr = stringRedisTemplate.opsForValue().get(PmsConstant.CATALOG_JSON);
            catalogMap = JSON.parseObject(catalogJsonStr, new TypeReference<Map<String, List<Catalog2Vo>>>(){});

        } else {
            System.out.println("查询数据库");
            catalogMap = getCatalogJsonByDb();
            String jsonString = JSON.toJSONString(catalogMap);
            stringRedisTemplate.opsForValue().set(PmsConstant.CATALOG_JSON, jsonString);
        }
       return catalogMap;
    }

    /**
     * 缓存未命中时采用分布式锁访问数据库，避免大量数据访问
     * @return
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        //1、占分布式锁。 设置过期时间必须和加锁是同步的，保证原子性（避免死锁）
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(PmsConstant.LOCK, uuid, 300, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lock)) {
            Map<String, List<Catalog2Vo>> catalogMap = null;
            try {
                System.out.println("当前线程获得分布式锁");
                catalogMap = getCatalogJson();
            } finally {
                //使用lua脚本解锁
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(luaScript, Long.class), Collections.singletonList(PmsConstant.LOCK), uuid);
                System.out.println("当前线程解锁");
            }
            return  catalogMap;
        } else {
            //休眠一段时间 重试
            System.out.println("获取分布式锁失败...等待重试");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //自旋锁方式
            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    /**
     * 用redisson分布式锁
     * 缓存数据库数据一致性问题，如何保证数据一致性
     * 1）双写模式 更新数据时 写入缓存 会有脏读数据 保证最终一致性
     * 2）失效模式 更新数据时 删除缓存 也会有脏数据 保证最终一致性
     * 写入数据时加锁 读写锁 效率更高
     * 中间件canal 根据binlog修改缓存 不用修改业务代码
     * @return
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        RLock lock = redissonClient.getLock(PmsConstant.CATALOG_LOCK);
        try {
            lock.lock();
            Map<String, List<Catalog2Vo>> catalogJson = getCatalogJson();
            return catalogJson;
        }finally {
            lock.unlock();
        }
    }

    private Map<String,List<Catalog2Vo>> getCatalogJsonByDb(){
        //只查询一次数据库
        List<CategoryEntity> categoryEntityList = lambdaQuery().list();

        //查询出所有一级分类目录
        List<CategoryEntity> level1Catalogs = getByParentCId(categoryEntityList, PmsConstant.CATALOG_1_PID);
        Map<String,List<Catalog2Vo> > catalogMap = new HashMap<>();
        level1Catalogs.forEach( category -> {
            Long catId = category.getCatId();
            //查出一级分类下的所有二级分类
            List<CategoryEntity> catalog2List = getByParentCId(categoryEntityList, category.getCatId());

            List<Catalog2Vo> catalog2Vos = catalog2List.stream().map(catalog2 -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                //查出二级分类下的所有三级分类
                List<CategoryEntity> catalog3List = getByParentCId(categoryEntityList, catalog2.getCatId());
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

    private List<CategoryEntity> getByParentCId(List<CategoryEntity> categoryEntityList, Long catId) {
        return categoryEntityList
                .stream()
                .filter(categoryEntity -> catId.equals(categoryEntity.getParentCid()))
                .collect(Collectors.toList());
    }


}
