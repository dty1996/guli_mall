package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.search.config.EsConfig;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchAppTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Test
    public void Test() {
        System.out.println(restHighLevelClient);
    }


    /**
     * 更新保存二合一
     * @throws IOException
     */
    @Test
    public void index() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
        User user = new User("张三", 23, "男");
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString,  XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(request, EsConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);
    }


    @Test
    public void searchIndex() throws IOException {
        SearchRequest searchRequest = new SearchRequest("users");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "张三"));
        System.out.println(searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, EsConfig.COMMON_OPTIONS);
        //获取hits
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            User user = JSONObject.parseObject(sourceAsString, User.class);
            System.out.println(user.toString());
        }
    }


}
