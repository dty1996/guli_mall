package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.Constants.SearchConstant;
import com.atguigu.gulimall.search.config.EsConfig;
import com.atguigu.gulimall.search.service.EsService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EsServiceImpl implements EsService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean indexSku(List<SkuEsModel> skuEsModelList) {

        BulkRequest bulkRequest = new BulkRequest();
        skuEsModelList.forEach(skuEsModel -> {
            String jsonString = JSONObject.toJSONString(skuEsModel);
            IndexRequest indexRequest = new IndexRequest(SearchConstant.PRODUCT_INDEX)
                    .id(skuEsModel.getSkuId().toString())
                    .source(jsonString);
            bulkRequest.add(indexRequest);
        });
        try {
            restHighLevelClient.bulk(bulkRequest, EsConfig.COMMON_OPTIONS);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
