package com.github.dajinbao.apiserver.service;

import com.github.dajinbao.apiserver.common.model.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ProductService {

    private final MongoTemplate template;

    public ProductService(MongoTemplate template) {
        this.template = template;
    }

    public Map getByCode(String productCode) {
        return template.findOne(query(where("productCode").is(productCode)), Map.class, "product");
    }

    public Page<Map> listBySellerId(String sellerId, int pageNo, int pageSize) {
        long totalCount = template.count(query(where("seller.sellerId").is(sellerId)), "product");

        Page<Map> page = Page.of(pageNo, pageSize, totalCount);
        List<Map> productList = template.find(query(where("seller.sellerId").is(sellerId)).skip((long) (pageNo - 1) * pageSize).limit(pageSize), Map.class, "product");
        page.setRecords(productList);
        return page;
    }

}
