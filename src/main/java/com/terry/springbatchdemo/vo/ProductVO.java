package com.terry.springbatchdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductVO {
    private String productId;
    private int productPrice;

    public ProductVO() {

    }
}
