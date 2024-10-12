package com.github.dajinbao.apiserver.service;

import cn.hutool.core.util.PageUtil;
import com.github.dajinbao.crawler.ImgDownloader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    TaskService taskService;
    @Autowired
    MongoTemplate template;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Test
    void create() {
        taskService.create("yahoo-product", "https://page.auctions.yahoo.co.jp/jp/auction/d1154523468");
    }

    @Test
    void test() {
        long total = template.count(new Query(), "product");
        int totalPage = PageUtil.totalPage(total, 10);
        for (int i = 1; i <= totalPage; i++) {
            Query query = new Query().skip((i - 1) * 10L).limit(10);
            List<Map> list = template.find(query, Map.class, "product");
            for (Map map : list) {
                String code = map.get("productCode").toString();
                String path = uploadDir + "productImg/yahoo/" + code + "/";
                List<String> imgList = (List<String>) map.get("productImgUrl");
                for (String img : imgList) {
                    ImgDownloader.download(img, path);
                }
            }
        }


    }

}