package com.atguigu;


import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ThirdPartAppTest{
    @Autowired
    private OSSClient ossClient;

    @Test
    public void contextUpload() throws FileNotFoundException {
        String filePath = "F:\\message.txt";
        FileInputStream fileInputStream = new FileInputStream(filePath);
        ossClient.putObject("gulimall-lgkj","123.txt", fileInputStream);
        ossClient.shutdown();

        System.out.println("上传完成");
    }
}
