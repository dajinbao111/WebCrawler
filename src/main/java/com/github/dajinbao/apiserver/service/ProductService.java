package com.github.dajinbao.apiserver.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dajinbao.apiserver.common.model.Page;
import com.github.dajinbao.apiserver.entity.ProductReq;
import com.github.dajinbao.apiserver.entity.ProductResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ProductService {
    @Value("${file.upload-dir}")
    private String uploadDir;
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
            query.addCriteria(where("productSite").is(req.getProductSite()));
        }
        if (StrUtil.isNotBlank(req.getSellerId())) {
            query.addCriteria(where("seller.sellerId").is(req.getSellerId()));
        }
        if (StrUtil.isNotBlank(req.getUpdatedStartTime()) && StrUtil.isNotBlank(req.getUpdatedEndTime())) {
            query.addCriteria(where("updatedAt")
                    .gte(DateUtil.parse(req.getUpdatedStartTime(), "yyyy-MM-dd").toLocalDateTime())
                    .lte(DateUtil.parse(req.getUpdatedEndTime(), "yyyy-MM-dd").toLocalDateTime()));
        }


        long totalCount = template.count(query, "product");

        Page<ProductResp> page = Page.of(pageNo, pageSize, totalCount);
        List<ProductResp> productList = template.find(query.with(Sort.by(Sort.Direction.DESC, "updatedAt")).skip((long) (pageNo - 1) * pageSize).limit(pageSize), Map.class, "product")
                .stream().map(m -> toResp(m, false)).collect(Collectors.toList());
        page.setRecords(productList);
        return page;
    }

    private ProductResp toResp(Map map, boolean isExport) {
        ProductResp resp = new ProductResp();
        resp.setId(Convert.toStr(map.get("_id")));
        resp.setProductSite(Convert.toStr(map.get("productSite")));
        resp.setProductCode(Convert.toStr(map.get("productCode")));
        //http://47.99.75.168:8081/productImg/yahoo/h1156551433/i-img800x800-17286103581270vy8pag47033.jpg
        resp.setProductMainImg(Convert.toStr(map.get("productMainImg")));
        if (isExport) {
            resp.setProductMainImg(convertImgPath(resp.getProductMainImg(), resp.getProductSite(), resp.getProductCode()));
        } else {
            resp.setProductMainImg(convertImgUrl(resp.getProductMainImg(), resp.getProductSite(), resp.getProductCode()));
        }
        resp.setProductPrice(Convert.toStr(map.get("productPrice")));
        resp.setProductTitle(Convert.toStr(map.get("productTitle")));
        resp.setProductUrl(Convert.toStr(map.get("productUrl")));
        resp.setUpdatedAt(DateUtil.format((Date) map.get("updatedAt"), "yyyy-MM-dd HH:mm:ss"));
        resp.setProductInfo(Convert.toStr(map.get("productInfo")));
        resp.setProductDesc(Convert.toStr(map.get("productDesc")));

        ArrayList<String> productThumbnails = (ArrayList<String>) map.get("productThumbnail");
        if (productThumbnails != null) {

            String thumbnails = productThumbnails.stream().map(url -> convertImgUrl(url, resp.getProductSite(), resp.getProductCode())).collect(Collectors.joining(";"));
            resp.setProductThumbnail(thumbnails);
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

    public String convertImgUrl(String url, String site, String code) {
        String fileName = StrUtil.subAfter(url, "/", true);
        if (FileNameUtil.extName(fileName).isBlank()) {
            fileName = fileName + ".jpg";
        }
        Path file = Paths.get(uploadDir + "/productImg/" + site + "/" + code + "/" + fileName);
        if (Files.exists(file)) {
            return "http://47.99.75.168:8081/productImg/" + site + "/" + code + "/" + fileName;
        } else {
            return url;
        }
    }

    public String convertImgPath(String url, String site, String code) {
        String fileName = StrUtil.subAfter(url, "/", true);
        if (FileNameUtil.extName(fileName).isBlank()) {
            fileName = fileName + ".jpg";
        }
        Path file = Paths.get(uploadDir + "/productImg/" + site + "/" + code + "/" + fileName);
        if (Files.exists(file)) {
            return file.toString();
        }
        return null;
    }


    public List<ProductResp> export(ProductReq req) {
        Query query = new Query();
        if (StrUtil.isNotBlank(req.getProductCode())) {
            query.addCriteria(where("productCode").is(req.getProductCode()));
        }
        if (StrUtil.isNotBlank(req.getProductSite())) {
            query.addCriteria(where("productSite").is(req.getProductSite()));
        }
        if (StrUtil.isNotBlank(req.getSellerId())) {
            query.addCriteria(where("seller.sellerId").is(req.getSellerId()));
        }
        if (StrUtil.isNotBlank(req.getUpdatedStartTime()) && StrUtil.isNotBlank(req.getUpdatedEndTime())) {
            query.addCriteria(where("updatedAt")
                    .gte(DateUtil.parse(req.getUpdatedStartTime(), "yyyy-MM-dd").toLocalDateTime())
                    .lte(DateUtil.parse(req.getUpdatedEndTime(), "yyyy-MM-dd").toLocalDateTime()));
        }

        List<ProductResp> productList = template.find(query.with(Sort.by(Sort.Direction.DESC, "updatedAt")).limit(10000), Map.class, "product")
                .stream().map(m -> toResp(m, true)).collect(Collectors.toList());
        return productList;
    }
}
