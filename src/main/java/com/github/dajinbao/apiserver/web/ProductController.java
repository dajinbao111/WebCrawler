package com.github.dajinbao.apiserver.web;

import com.github.dajinbao.apiserver.common.model.Page;
import com.github.dajinbao.apiserver.common.model.RestResult;
import com.github.dajinbao.apiserver.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "商品维护管理")
@RestController
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "商品ID查询商品信息")
    @GetMapping("/v1/products/{productCode}")
    public RestResult<Map> get(@PathVariable String productCode) {
        return RestResult.success(service.getByCode(productCode));
    }

    @Operation(summary = "店铺ID查询商品信息")
    @GetMapping("/v1/products/seller/{sellerId}")
    public RestResult<Page<Map>> listBySellerId(@PathVariable String sellerId, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize) {
        return RestResult.success(service.listBySellerId(sellerId, pageNo, pageSize));
    }
}
