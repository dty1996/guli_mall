package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.Constants.SearchConstant;
import com.atguigu.gulimall.search.config.EsConfig;
import com.atguigu.gulimall.search.entity.params.SearchParam;
import com.atguigu.gulimall.search.entity.vo.AttrResponseVo;
import com.atguigu.gulimall.search.entity.vo.SearchResponseVo;
import com.atguigu.gulimall.search.feign.ProductFeignService;
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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dty
 * @date 2022/9/6
 * @dec ss
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {


    @Autowired
    private RestHighLevelClient client;


    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResponseVo search(SearchParam searchParam) {

        SearchRequest request = buildSearchRequest(searchParam);
        SearchResponseVo searchResponseVo = null;
        try {
            //2.es检索
            SearchResponse searchResponse = client.search(request, EsConfig.COMMON_OPTIONS);

            //3将响应结果封装成需要的数据结构：searchResponse
            searchResponseVo = toSearchResponseVo(searchResponse, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponseVo;
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
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(SearchConstant.SKU_TITLE);
            highlightBuilder.preTags(SearchConstant.PRE_TAGS);
            highlightBuilder.postTags(SearchConstant.POST_TAGS);
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * 分页
         */
        int from = SearchConstant.DEFAULT_NUM;
        if (null != searchParam.getPageNum()) {
           from  = (searchParam.getPageNum() - 1) * SearchConstant.SIZE;
        }
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(SearchConstant.SIZE);

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
                if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
                    HighlightField highlightField = hit.getHighlightFields().get(SearchConstant.SKU_PRICE);
                    if (null != highlightField) {
                        skuEsModel.setSkuTitle( highlightField.getFragments()[0].string());
                    }

                }
                skuEsModels.add(skuEsModel);
            }
        }
        searchResponseVo.setProduct(skuEsModels);

        //分页信息
//        Integer pageNum = searchParam.getPageNum() == null ? 0: searchResponseVo.getPageNum();
//
//        long total = hits.getTotalHits().value;
//        Integer totalPage = Math.toIntExact(total / SearchConstant.SIZE );
//        searchResponseVo.setPageNum(pageNum);
//        searchResponseVo.setTotal(total);
//        searchResponseVo.setTotalPage(totalPage);

        //聚合信息
        //属性聚合信息
        List<SearchResponseVo.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrsAgg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResponseVo.AttrVo attrVo = new SearchResponseVo.AttrVo();
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            ArrayList<String> values = new ArrayList<>();
            for (Terms.Bucket valueBucket : attrValueAgg.getBuckets()) {
                values.add(valueBucket.getKeyAsString());
            }
            attrVo.setAttrValue(values);
            attrVos.add(attrVo);
        }
        searchResponseVo.setAttrs(attrVos);

        //目录信息

        List<SearchResponseVo.CatalogVo> catalogVos  = new ArrayList<>();
        ParsedLongTerms catalogAgg = searchResponse.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket :  buckets) {
            SearchResponseVo.CatalogVo catalogVo = new SearchResponseVo.CatalogVo();
            String catalogIdStr = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catalogIdStr));
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            Terms.Bucket catalogBucket = catalogNameAgg.getBuckets().get(0);
            catalogVo.setCatalogName(catalogBucket.getKeyAsString());
            catalogVos.add(catalogVo);
        }
        searchResponseVo.setCatalogs(catalogVos);


        //品牌信息
        List<SearchResponseVo.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = searchResponse.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResponseVo.BrandVo brandVo = new SearchResponseVo.BrandVo();
            String brandIdStr = bucket.getKeyAsString();
            brandVo.setBrandId(Long.parseLong(brandIdStr));
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            Terms.Bucket brandNameBucket = brandNameAgg.getBuckets().get(0);
            brandVo.setBrandName(brandNameBucket.getKeyAsString());
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            Terms.Bucket brandImgBucket = brandImgAgg.getBuckets().get(0);
            brandVo.setBrandImg(brandImgBucket.getKeyAsString());

            brandVos.add(brandVo);
        }
        searchResponseVo.setBrands(brandVos);

        //5、分页信息-页码
        Integer pagePum = searchParam.getPageNum() == null? 1: searchParam.getPageNum();
        searchResponseVo.setPageNum(pagePum);
        //5、1分页信息、总记录数
        long total = hits.getTotalHits().value;
        searchResponseVo.setTotal(total);

        //5、2分页信息-总页码-计算
        int totalPages = (int)total % SearchConstant.SIZE == 0 ?
                (int)total / SearchConstant.SIZE  : ((int)total / SearchConstant.SIZE  + 1);
        searchResponseVo.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResponseVo.setPageNavs(pageNavs);


        //6、构建面包屑导航
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            List<SearchResponseVo.NavVo> collect = searchParam.getAttrs().stream().map(attr -> {
                //1、分析每一个attrs传过来的参数值
                SearchResponseVo.NavVo navVo = new SearchResponseVo.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.info(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                //拿到所有的查询条件，去掉当前
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr,"UTF-8");
                    encode.replace("+","%20");  //浏览器对空格的编码和Java不一样，差异化处理
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = searchParam.get_queryString().replace("&attrs=" + attr, "");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            searchResponseVo.setNavs(collect);
        }
        return searchResponseVo;
    }

}
