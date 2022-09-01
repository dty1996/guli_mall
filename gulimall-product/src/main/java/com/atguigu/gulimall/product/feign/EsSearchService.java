package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author dty
 * @date 2022/8/31
 * @dec 描述
 */
@FeignClient("searchApp")
public interface EsSearchService {
    @PostMapping("/es/index/sku")
    R indexSku(@RequestBody List<SkuEsModel> skuEsModelList);
}
