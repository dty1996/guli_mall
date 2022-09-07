package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.Constants.SearchConstant;
import com.atguigu.gulimall.search.config.EsConfig;
import com.atguigu.gulimall.search.entity.params.SearchParam;
import com.atguigu.gulimall.search.entity.vo.SearchResponseVo;
import com.atguigu.gulimall.search.service.MallSearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author dty
 * @date 2022/9/6
 * @dec 描述
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {


    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        //1.准备检索请求
        SearchRequest request = new SearchRequest();


        try {
            //2.es检索
            SearchResponse searchResponse = client.search(request, EsConfig.COMMON_OPTIONS);

            //3将响应结果封装成需要的数据结构：searchResponse
            SearchResponseVo searchResponseVo = toSearchResponseVo(searchResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 准备检索请求
     * 模糊匹配 ，过滤（按照属性，分类， 品牌， 价格区间， 库存），排序，分页，高亮，聚合分析
     * @param searchParam
     * @return SearchRequest
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam){
        //构建查询dsl
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /**
         *  查询：模糊匹配，过滤
         */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //must模糊匹配
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(SearchConstant.SKU_TITLE, searchParam.getKeyword()));
        }
        //filter,过滤
        if (null != searchParam.getCatalog3Id()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(SearchConstant.CATALOG_ID, searchParam.getCatalog3Id()));
        }
        if (null != searchParam.getBrandIds() && searchParam.getBrandIds().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(SearchConstant.BRAND_ID, searchParam.getBrandIds()));
        }
        if (null != searchParam.getHasStock()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(SearchConstant.HAS_STOCK, searchParam.getHasStock() == 1));
        }

        /**
         * 按照属性值查询
         */
        if (null != searchParam.getAttrs() && searchParam.getAttrs().size() > 0) {

//            query.must(QueryBuilders.termsQuery(SearchConstant.ATTRS_ATTR_ID));
            //attrs=1_5寸:8寸&attrs=2_8g:16g
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] strings = attr.split(SearchConstant.ATTR_SPILT);
                String attrId = strings[0];
                String[] attrValues = strings[1].split(SearchConstant.ATTR_VALUE_SPILT);
                nestedBoolQuery.must(QueryBuilders.termQuery(SearchConstant.ATTRS_ATTR_ID, attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery(SearchConstant.ATTRS_ATTR_VALUE, attrValues));

                //每查一次属性需要匹配一次聚合查询
                boolQueryBuilder.filter(QueryBuilders.nestedQuery(SearchConstant.PATH, nestedBoolQuery, ScoreMode.None));
            }

        }

        //按照价格区间 价格区间格式：1_500/0_500
        if (StringUtils.isNotEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(SearchConstant.SKU_PRICE);
            String[] prices = searchParam.getSkuPrice().split(SearchConstant.PRICE_SPILT);
            if (prices.length == 2) {
                //区间
                rangeQuery.gte(prices[0]).lte(prices[1]);
            } else {
                if (searchParam.getSkuPrice().startsWith(SearchConstant.PRICE_SPILT)) {
                    //大于
                    rangeQuery.lte(prices[0]);
                } else {
                    //小于
                    rangeQuery.gte(prices[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        //将查询条件进行封装
        searchSourceBuilder.query(boolQueryBuilder);



        return null;
    }

    private SearchResponseVo toSearchResponseVo(SearchResponse searchResponse) {
        return null;
    }

}
