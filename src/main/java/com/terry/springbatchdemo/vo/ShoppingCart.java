package com.terry.springbatchdemo.vo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShoppingCart {
    private String userId;
    List<ShoppingItem> shoppingItemList = new ArrayList<>();
    private long totalPrice;

    public ShoppingCart() {

    }

    public ShoppingCart(String userId, List<ShoppingItem> shoppingItemList) {
        this.userId = userId;
        this.shoppingItemList = shoppingItemList;
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        totalPrice = 0L;
        shoppingItemList.stream().forEach(
                shoppingItem -> {
                    Product product = shoppingItem.getProduct();
                    int productPrice = product.getProductPrice();
                    int totalProductPrice = productPrice * shoppingItem.getCnt();
                    totalPrice += totalProductPrice;
                }
        );
    }
}
