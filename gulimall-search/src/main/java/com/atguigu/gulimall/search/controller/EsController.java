package com.atguigu.gulimall.search.controller;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.EsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author dty
 * @date 2022/8/31
 * @dec 描述
 */
@RestController
@RequestMapping("es/")
public class EsController {

    @Autowired
    private EsService esService;

    @PostMapping("index/sku")
    public R indexSku(@RequestBody List<SkuEsModel> skuEsModelList){
        boolean sku = esService.indexSku(skuEsModelList);
        if (sku) {
            return R.ok();
        }
        return R.error();
    }
}
