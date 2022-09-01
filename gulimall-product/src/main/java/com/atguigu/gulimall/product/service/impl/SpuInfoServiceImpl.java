package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.exception.RRException;
import com.atguigu.common.to.SkuReduceTo;
import com.atguigu.common.to.SkuStockVo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.constants.PmsConstant;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.entity.params.*;
import com.atguigu.gulimall.product.enums.SearchTypeEnum;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.EsSearchService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO 高级部分优化
 */

@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDao spuInfoDao;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private EsSearchService esSearchService;


    /**
     * 按条件查询商品信息
     * 从map中取数据时，先转成String
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {


        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String)params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and( (w) ->{
                w.like(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key);
            });
        }
        String catelogId = (String)params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
        }
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            queryWrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }
        String brandId = (String)params.get("brandId");
        if (StringUtils.isNotEmpty(brandId)) {
            queryWrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品信息
     * @param spuAddParam
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void saveSpu(SpuAddParam spuAddParam) {

        //保存spuInfo信息
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(spuAddParam, spuInfo);
        save(spuInfo);

        //保存spuInfo-desc
        List<String> decriptList = spuAddParam.getDecript();
        String descript = String.join(",", decriptList);
       SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
       spuInfoDescEntity.setDecript(descript);
       spuInfoDescEntity.setSpuId(spuInfo.getId());
       spuInfoDescService.save(spuInfoDescEntity);


        //保存spuInfo-image
        List<String> images = spuAddParam.getImages();
        List<SpuImagesEntity> imageArrayList =
        images.stream().map( per -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfo.getId());
            spuImagesEntity.setImgUrl(per);
            return spuImagesEntity;
        }).collect(Collectors.toList());

        spuImagesService.saveBatch(imageArrayList);

        //保存商品属性值 spu-attr-value
        List<BaseAttr> baseAttrs = spuAddParam.getBaseAttrs();
        List<ProductAttrValueEntity> attrValueList  =  baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValue = new ProductAttrValueEntity();
            BeanUtils.copyProperties(baseAttr, productAttrValue);
            productAttrValue.setSpuId(spuInfo.getId());
           return productAttrValue;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(attrValueList);

        //保存商品积分信息
        Bounds bounds = spuAddParam.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfo.getId());
            //open-feign调用
        R saveBounds = couponFeignService.saveBounds(spuBoundsTo);
        if (!saveBounds.getCode().equals(0)) {
            log.error("远程调用保存信息失败");
        }


        //保存sku信息
        List<Sku> skus = spuAddParam.getSkus();
        skus.forEach(sku -> {

            //保存skuInfo信息
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
              //初始化skuInfo信息
            skuInfoEntity.setSpuId(spuInfo.getId());
            skuInfoEntity.setBrandId(spuAddParam.getBrandId());
            skuInfoEntity.setCatalogId(spuAddParam.getCatalogId());
            skuInfoEntity.setSaleCount(PmsConstant.INIT_COUNT);
            skuInfoService.save(skuInfoEntity);

            //保存商品优惠信息
            SkuReduceTo skuReduceTo = new SkuReduceTo();
            BeanUtils.copyProperties(sku, skuReduceTo);
            skuReduceTo.setSkuId(skuInfoEntity.getSkuId());
            if (sku.getFullCount() > 0 || sku.getDiscount().compareTo(new BigDecimal("0")) ==1) {
                R r = couponFeignService.saveSkuReduceInfo(skuReduceTo);
                if (!r.getCode().equals(0)) {
                    log.error("远程调用保存优惠信息失败");
                }
            }



            //保存skuImage信息
            List<Images> skuImages = sku.getImages();
            List<SkuImagesEntity> skuImagesList =
            skuImages.stream().map(skuImage -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                BeanUtils.copyProperties(skuImage, skuImagesEntity);
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuImagesEntity;
            }).filter(per -> StringUtils.isNotEmpty(per.getImgUrl()))
                    .collect(Collectors.toList());

            skuImagesService.saveBatch(skuImagesList);

            //保存属性值信息
            List<Attr> skuAttrs = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueList =
            skuAttrs.stream().map(skuAttr -> {
                SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(skuAttr, skuSaleAttrValue);
                skuSaleAttrValue.setSkuId(skuInfoEntity.getSkuId());
                return skuSaleAttrValue;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
        });

    }

    /**
     * 商品上架
     * @param spuId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void up(Long spuId) {
        //根据spuInfo 查出所有的sku信息；
        List<SkuInfoEntity> skus =  skuInfoService.getSkusBySpuId(spuId);
        //根据spuId查询attr—value 可供检索的
        List<ProductAttrValueEntity> attrs = productAttrValueService.queryBySpu(spuId);
        List<Long> attrIds = attrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> attrIdsIndex = attrIds.stream().filter(id -> {
            Integer count = attrService.lambdaQuery().eq(AttrEntity::getAttrId, id).eq(AttrEntity::getSearchType, SearchTypeEnum.CAN).count();
            return count > 0 ;
        }).collect(Collectors.toList());
        List<SkuEsModel.Attr> attrList = attrs.stream().filter( attr ->
                attrIdsIndex.contains(attr.getAttrId())
        ).map(item -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());

        //远程调用查询库存信息
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        List<SkuStockVo> skuStockVos ;
        try {
            skuStockVos = wareFeignService.queryStockBySku(skuIds);
        } catch (Exception e) {
            skuStockVos = skuIds.stream().map(skuId -> {
                SkuStockVo skuStockVo = new SkuStockVo();
                skuStockVo.setSkuId(skuId);
                skuStockVo.setHasStock(true);
                return skuStockVo;
            }).collect(Collectors.toList());
            e.printStackTrace();
        }
        Map<Long, Boolean> hasStockMap = skuStockVos.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            ////TODO 检查有无遗漏信息
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            //不同的属性 skuImg, skuPrice,hasStock,hotScore, catalogName, brandName
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setHotScore(Constant.DEFAULT_SCORE);
            //brandName
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            //catalogName
            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            //skuId
            skuEsModel.setHasStock(hasStockMap.get(sku.getSkuId()));
            skuEsModel.setAttrs(attrList);
            return skuEsModel;
        }).collect(Collectors.toList());
        System.out.println(skuEsModels.toString());
        ////TODO 失败，重复调用问题？接口幂等性： 重复调用机制
        R r = esSearchService.indexSku(skuEsModels);
        if (!Constant.DEFAULT_SUCCESS_CODE.equals(r.getCode())) {
            throw new RRException(BizExceptionEnum.PRODUCT_UP_FAiL);
        }
        ////TODO 成功更新商品上架状态


    }
}
