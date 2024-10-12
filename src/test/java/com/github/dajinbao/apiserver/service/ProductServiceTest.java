package com.github.dajinbao.apiserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    MongoTemplate template;

    @Test
    void test() {

        Map productMap = template.findOne(query(where("productCode").is("d1154523468a")), Map.class, "product");
        System.out.println(productMap);
    }
}
