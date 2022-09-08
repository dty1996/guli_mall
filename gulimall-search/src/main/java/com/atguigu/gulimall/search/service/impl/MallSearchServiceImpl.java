package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.to.es.SkuEsModel;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dty
 * @date 2022/9/6
 * @dec ss
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {


    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResponseVo search(SearchParam searchParam) {

        SearchRequest request = buildSearchRequest(searchParam);

        try {
            //2.es检索
            SearchResponse searchResponse = client.search(request, EsConfig.COMMON_OPTIONS);

            //3将响应结果封装成需要的数据结构：searchResponse
            SearchResponseVo searchResponseVo = toSearchResponseVo(searchResponse, searchParam);
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
        if (null != searchParam.getBrandId() && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(SearchConstant.BRAND_ID, searchParam.getBrandId()));
        }
        if (null != searchParam.getHasStock()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(SearchConstant.HAS_STOCK, searchParam.getHasStock() == 1));
        }

        /**
         * 按照属性值查询
         */
        if (null != searchParam.getAttrs() && searchParam.getAttrs().size() > 0) {

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

        /**
         * 排序
         */
        if (StringUtils.isNotEmpty(searchParam.getSort())) {
            String[] split = searchParam.getSort().split(SearchConstant.SORT_SPILT);
            String field = split[0];
            String sortMode = split[1];
            if (SearchConstant.SORT_ORDER_ASC.equals(sortMode)) {
                searchSourceBuilder.sort(field, SortOrder.ASC);
            } else {
                searchSourceBuilder.sort(field, SortOrder.DESC);
            }

        }
        /**
         * 高亮显示
         */
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(SearchConstant.SKU_TITLE);
        highlightBuilder.preTags(SearchConstant.PRE_TAGS);
        highlightBuilder.postTags(SearchConstant.POST_TAGS);
        searchSourceBuilder.highlighter(highlightBuilder);

        /**
         * 分页
         */
        int from = SearchConstant.DEFAULT_NUM;
        if (null != searchParam.getPageNum()) {
           from  = (searchParam.getPageNum() - 1) * SearchConstant.SIZE;
        }
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(SearchConstant.SIZE);

        System.out.println("DSL" + searchSourceBuilder);
        /**
         * 聚合
         */

        //1.品牌聚合
        TermsAggregationBuilder brandAggBuilder = AggregationBuilders.terms("brand_agg").field("brandId").size(10);
        brandAggBuilder.subAggregation( AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAggBuilder.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brandAggBuilder);

        //2.分类聚合
        TermsAggregationBuilder catalogAggBuilder = AggregationBuilders.terms("catalog_agg").field("catalogId").size(10);
        catalogAggBuilder.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAggBuilder);

        //3属性聚合 嵌套聚合
        NestedAggregationBuilder attrsAggBuilder = AggregationBuilders.nested("attr_agg", SearchConstant.PATH);
        TermsAggregationBuilder attrIdAggBuilder = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attrIdAggBuilder.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10));
        attrIdAggBuilder.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        attrsAggBuilder.subAggregation(attrIdAggBuilder);

        searchSourceBuilder.aggregation(attrsAggBuilder);
        System.out.println("构建的dsl" + searchSourceBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(SearchConstant.PRODUCT_INDEX).source(searchSourceBuilder);
        return searchRequest;
    }

    /**
     * 将es查询结果转为SearchResponse对象
     * @param searchResponse
     * @param searchParam
     * @return
     */
    private SearchResponseVo toSearchResponseVo(SearchResponse searchResponse, SearchParam searchParam) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();

        //获取product属性
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length > 0) {
            for (SearchHit hit: searchHits) {
                //将数据转成skuModel对象
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSONObject.parseObject(sourceAsString, SkuEsModel.class);
                skuEsModels.add(skuEsModel);
            }
        }
        searchResponseVo.setProducts(skuEsModels);

        //分页信息
        Integer pageNum = searchParam.getPageNum();
        long total = hits.getTotalHits().value;
        Integer totalPage = Math.toIntExact(total / pageNum + 1);
        searchResponseVo.setPageNum(pageNum);
        searchResponseVo.setTotal(total);

        ////TODO 补充聚合属性
        //聚合信息
        //属性聚合信息
        List<SearchResponseVo.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrsAgg = searchResponse.getAggregations().get("attrs_agg");


        //目录信息
        ParsedLongTerms catalogAgg = searchResponse.getAggregations().get("catalog_agg");
        SearchResponseVo.CatalogVo catalogVo = new SearchResponseVo.CatalogVo();

        //品牌信息
        List<SearchResponseVo.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = searchResponse.getAggregations().get("brand_agg");
        return null;
    }

}
