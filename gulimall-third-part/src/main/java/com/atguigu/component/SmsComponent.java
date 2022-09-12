package com.atguigu.component;

import com.atguigu.utils.HttpUtils;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 描述
 */
@ConfigurationProperties(prefix = "spring.alicloud.sms")
@Component
@Data
public class SmsComponent {

    private String host;
    private String path;
    private String templatedId;
    private String smsSignId;
    private String appcode;

    public void sendCode(String phone, String code) {
        String method = "POST";
        String appcode = "784dc4b6acc24f7aa8c3f2f62b903741";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appcode);
        String param;

        param = "**code**:" + code + "," +  "**minute**:30";

        Map<String, String> queries = new HashMap<>();
        queries.put("mobile", phone);
        queries.put("param", param);
        queries.put("smsSignId", smsSignId);
        queries.put("templateId", templatedId);
        Map<String, String> bodies = new HashMap<>();


        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, queries, bodies);
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
