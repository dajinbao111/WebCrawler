package com.github.dajinbao.apiserver.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.converters.string.StringImageConverter;
import lombok.Data;

@Data
@ContentRowHeight(40)
@ColumnWidth(100 / 8)
public class ProductResp {

    @ExcelIgnore
    private String id;
    @ExcelProperty("更新时间")
    private String updatedAt;
    @ExcelProperty("站点")
    private String productSite;
    @ExcelProperty("商品ID")
    private String productCode;
    @ExcelProperty("商品名称")
    private String productTitle;
    @ExcelProperty("商品地址")
    private String productUrl;
    @ExcelProperty(value = "商品主图", converter = StringImageConverter.class)
    private String productMainImg;
    @ExcelProperty("商品缩略图")
    private String productThumbnail;
    @ExcelProperty("商品价格")
    private String productPrice;
    @ExcelProperty("一级类目名称")
    private String category1Name;
    @ExcelProperty("一级类目地址")
    private String category1Url;
    @ExcelProperty("二级类目名称")
    private String category2Name;
    @ExcelProperty("二级类目地址")
    private String category2Url;
    @ExcelProperty("三级类目名称")
    private String category3Name;
    @ExcelProperty("三级类目地址")
    private String category3Url;
    @ExcelProperty("四级类目名称")
    private String category4Name;
    @ExcelProperty("四级类目地址")
    private String category4Url;
    @ExcelProperty("商品信息")
    private String productInfo;
    @ExcelProperty("商品描述")
    private String productDesc;
    @ExcelProperty("卖家ID")
    private String sellerId;
    @ExcelProperty("卖家名称")
    private String sellerName;
    @ExcelProperty("卖家店铺")
    private String sellerUrl;

}
