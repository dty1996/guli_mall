package com.atguigu.gulimall.search.config;

import com.atguigu.gulimall.search.Constants.SearchConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * @author Administrator
 */
@Slf4j
@Configuration
public class EsConfig {

    @Value("classpath:json/skuMapping.json")
    private Resource resource;


    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("114.132.70.228", 9200, "http"))
        );
    }

    /**
     * 初始化
     * @throws IOException
     */
    @PostConstruct
    public void initProductMapping() throws IOException {
        //判断索引是否存在
        RestHighLevelClient restHighLevelClient = restHighLevelClient();
        GetIndexRequest indexRequest = new GetIndexRequest(SearchConstant.PRODUCT_INDEX);
        boolean exists =  restHighLevelClient.indices().exists(indexRequest, RequestOptions.DEFAULT);
        //存在创建索引并创建mapping
        if (!exists) {
            File file = resource.getFile();
            String skuMappingJson = FileUtils.readFileToString(file);
            log.info("json:{}", skuMappingJson);
            CreateIndexRequest request = new CreateIndexRequest(SearchConstant.PRODUCT_INDEX);
            request.mapping(skuMappingJson, XContentType.JSON);
            restHighLevelClient.indices().create(request, COMMON_OPTIONS);
        }

    }


}
