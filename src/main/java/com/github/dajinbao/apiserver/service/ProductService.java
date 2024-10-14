package com.github.dajinbao.apiserver.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dajinbao.apiserver.common.model.Page;
import com.github.dajinbao.apiserver.entity.ProductReq;
import com.github.dajinbao.apiserver.entity.ProductResp;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public Page<ProductResp> listByPage(ProductReq req, int pageNo, int pageSize) {
        Query query = new Query();
        if (StrUtil.isNotBlank(req.getProductCode())) {
            query.addCriteria(where("productCode").is(req.getProductCode()));
        }
        if (StrUtil.isNotBlank(req.getProductSite())) {
            query.addCriteria(Criteria.where("productSite").is(req.getProductSite()));
        }
        if (StrUtil.isNotBlank(req.getSellerId())) {
            query.addCriteria(where("seller.sellerId").is(req.getSellerId()));
        }
        if (req.getUpdatedStartTime() != null && req.getUpdatedEndTime() != null) {
            query.addCriteria(where("updatedAt")
                    .gte(DateUtil.parse(req.getUpdatedStartTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime())
                    .lte(DateUtil.parse(req.getUpdatedEndTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime()));
        }


        long totalCount = template.count(query, "product");

        Page<ProductResp> page = Page.of(pageNo, pageSize, totalCount);
        List<ProductResp> productList = template.find(query.skip((long) (pageNo - 1) * pageSize).limit(pageSize), Map.class, "product")
                .stream().map(this::toResp).collect(Collectors.toList());
        page.setRecords(productList);
        return page;
    }

    private ProductResp toResp(Map map) {
        ProductResp resp = new ProductResp();
        resp.setId(Convert.toStr(map.get("_id")));
        resp.setProductSite(Convert.toStr(map.get("productSite")));
        resp.setProductCode(Convert.toStr(map.get("productCode")));
        resp.setProductMainImg(Convert.toStr(map.get("productMainImg")));
        resp.setProductPrice(Convert.toStr(map.get("productPrice")));
        resp.setProductTitle(Convert.toStr(map.get("productTitle")));
        resp.setProductUrl(Convert.toStr(map.get("productUrl")));
        resp.setUpdatedAt(DateUtil.format((Date) map.get("updatedAt"), "yyyy-MM-dd HH:mm:ss"));
        resp.setProductInfo(Convert.toStr(map.get("productInfo")));
        resp.setProductDesc(Convert.toStr(map.get("productDesc")));

        ArrayList<String> productThumbnails = (ArrayList<String>) map.get("productThumbnail");
        if (productThumbnails != null) {
            resp.setProductThumbnail(String.join(";", productThumbnails));
        }
        ArrayList categories = (ArrayList) map.get("category");
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                LinkedHashMap category = (LinkedHashMap) categories.get(i);
                if (i == 0) {
                    resp.setCategory1Name(Convert.toStr(category.get("categoryName")));
                    resp.setCategory1Url(Convert.toStr(category.get("categoryUrl")));
                } else if (i == 1) {
                    resp.setCategory2Name(Convert.toStr(category.get("categoryName")));
                    resp.setCategory2Url(Convert.toStr(category.get("categoryUrl")));
                } else if (i == 2) {
                    resp.setCategory3Name(Convert.toStr(category.get("categoryName")));
                    resp.setCategory3Url(Convert.toStr(category.get("categoryUrl")));
                } else if (i == 3) {
                    resp.setCategory4Name(Convert.toStr(category.get("categoryName")));
                    resp.setCategory4Url(Convert.toStr(category.get("categoryUrl")));
                }
            }
        }
        LinkedHashMap seller = (LinkedHashMap) map.get("seller");
        if (seller != null) {
            resp.setSellerId(Convert.toStr(seller.get("sellerId")));
            resp.setSellerName(Convert.toStr(seller.get("sellerName")));
            resp.setSellerUrl(Convert.toStr(seller.get("sellerUrl")));
        }

        return resp;
    }

    public List<ProductResp> export(ProductReq req) {
        Query query = new Query();
        if (StrUtil.isNotBlank(req.getProductCode())) {
            query.addCriteria(where("productCode").is(req.getProductCode()));
        }
        if (StrUtil.isNotBlank(req.getProductSite())) {
            query.addCriteria(Criteria.where("productSite").is(req.getProductSite()));
        }
        if (StrUtil.isNotBlank(req.getSellerId())) {
            query.addCriteria(where("seller.sellerId").is(req.getSellerId()));
        }
        if (req.getUpdatedStartTime() != null && req.getUpdatedEndTime() != null) {
            query.addCriteria(where("updatedAt")
                    .gte(DateUtil.parse(req.getUpdatedStartTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime())
                    .lte(DateUtil.parse(req.getUpdatedEndTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime()));
        }

        List<ProductResp> productList = template.find(query, Map.class, "product")
                .stream().map(this::toResp).collect(Collectors.toList());
        return productList;
    }
}
