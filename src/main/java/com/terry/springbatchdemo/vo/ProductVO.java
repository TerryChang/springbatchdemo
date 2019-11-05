package com.terry.springbatchdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductVO {
    private Long idx;
    private String productName;
    private int productPrice;

    @Builder
    public ProductVO(Long idx, String productName, int productPrice) {
        this.idx = idx;
        this.productName = productName;
        this.productPrice = productPrice;
    }
}