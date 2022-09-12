package com.atguigu.gulimall.product;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.vo.SkuAttrVo;
import com.atguigu.gulimall.product.entity.vo.SkuItemVo;
import com.atguigu.gulimall.product.entity.vo.SpuAttrVo;
import com.atguigu.gulimall.product.entity.vo.SpuItemAttrGroupVo;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.service.SpuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = ProductApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class Tests {

    @Autowired
    private BrandService brandService;
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuInfoDao skuInfoDao;
    @Test
    public void userTest(){
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("华为");
        brandEntity.setLogo("huawei");
        boolean save = brandService.save(brandEntity);
        if (save) {
            log.info("success");
        } else {
            log.info("fail");
        }
    }

    @Test
    public void groupTest() {
        List<SpuItemAttrGroupVo> spuAttrVos = attrGroupService.selectSpuAttrsByCatalogId(225L, 11L);
        System.out.println(spuAttrVos.toString());
    }

    @Test
    public void saleAttrTest() {
        List<SkuAttrVo> skuAttrVos = skuInfoDao.selectSaleAttrs(11L);
        System.out.println(skuAttrVos.toString());
    }

    @Test
    public void skuItemTest(){
        SkuItemVo item = skuInfoService.item(1L);
        System.out.println(item.toString());
    }
}
