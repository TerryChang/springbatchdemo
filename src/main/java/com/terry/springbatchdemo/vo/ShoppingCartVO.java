package com.terry.springbatchdemo.vo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShoppingCartVO {
    private String loginId;
    List<ShoppingItemVO> shoppingItemList = new ArrayList<>();
    private long totalPrice;

    public ShoppingCartVO() {

    }

    public ShoppingCartVO(String loginId, List<ShoppingItemVO> shoppingItemVOList, long totalPrice) {
        this.loginId = loginId;
        this.shoppingItemList = shoppingItemVOList;
        this.totalPrice = totalPrice;
    }

}
