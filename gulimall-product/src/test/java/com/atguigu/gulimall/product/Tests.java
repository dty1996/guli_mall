package com.atguigu.gulimall.product;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ProductApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class Tests {

    @Autowired
    private BrandService brandService;

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
}
