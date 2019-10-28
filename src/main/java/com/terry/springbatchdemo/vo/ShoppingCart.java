package com.terry.springbatchdemo.vo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShoppingCart {
    private String loginId;
    List<ShoppingItem> shoppingItemList = new ArrayList<>();
    private long totalPrice;

    public ShoppingCart() {

    }

    public ShoppingCart(String loginId, List<ShoppingItem> shoppingItemList) {
        this.loginId = loginId;
        this.shoppingItemList = shoppingItemList;
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        totalPrice = 0L;
        shoppingItemList.forEach(
                shoppingItem -> {
                    totalPrice += shoppingItem.getTotalPriceByProduct();
                }
        );
    }
}
