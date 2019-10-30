package com.terry.springbatchdemo.vo;

import lombok.Getter;

@Getter
public class ShoppingItemVO {
    private ProductVO productVO;
    private int cnt;
    private int totalPriceByProduct;

    public ShoppingItemVO() {

    }

    public ShoppingItemVO(ProductVO productVO, int cnt) {
        this.productVO = productVO;
        this.cnt = cnt;
        this.totalPriceByProduct = productVO.getProductPrice() * cnt;
    }
}
