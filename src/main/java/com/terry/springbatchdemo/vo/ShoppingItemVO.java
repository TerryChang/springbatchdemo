package com.terry.springbatchdemo.vo;

import lombok.Getter;

@Getter
public class ShoppingItemVO {
    private ProductVO product;
    private int cnt;
    private int totalPriceByProduct;

    public ShoppingItemVO() {

    }

    public ShoppingItemVO(ProductVO productVO, int cnt, int totalPriceByProduct) {
        this.product = productVO;
        this.cnt = cnt;
        this.totalPriceByProduct = totalPriceByProduct;
    }
}
