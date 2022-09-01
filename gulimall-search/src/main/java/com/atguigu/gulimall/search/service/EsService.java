package com.atguigu.gulimall.search.service;


import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface EsService {

    boolean indexSku(List<SkuEsModel> skuEsModelList);
}
