package com.github.dajinbao.apiserver.web;

import com.alibaba.excel.EasyExcel;
import com.github.dajinbao.apiserver.common.model.Page;
import com.github.dajinbao.apiserver.common.model.RestResult;
import com.github.dajinbao.apiserver.entity.ProductReq;
import com.github.dajinbao.apiserver.entity.ProductResp;
import com.github.dajinbao.apiserver.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "商品维护管理")
@RestController
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

//    @Operation(summary = "商品ID查询商品信息")
//    @GetMapping("/v1/products/{productCode}")
//    public RestResult<Map> get(@PathVariable String productCode) {
//        return RestResult.success(service.getByCode(productCode));
//    }

    @Operation(summary = "查询商品信息")
    @PostMapping("/v1/products")
    public RestResult<Page<ProductResp>> listByPage(@RequestBody ProductReq req, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize) {
        return RestResult.success(service.listByPage(req, pageNo, pageSize));
    }

    @Operation(summary = "导出商品信息")
    @PostMapping("/v1/products/export")
    public void export(@RequestBody ProductReq req, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ProductResp.class).sheet().doWrite(service.export(req));
    }
}
