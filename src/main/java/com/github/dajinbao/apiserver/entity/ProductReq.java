package com.github.dajinbao.apiserver.entity;

import lombok.Data;

@Data
public class ProductReq {

    private String productSite;
    private String productCode;
    private String sellerId;
    private String updatedStartTime;
    private String updatedEndTime;
}
