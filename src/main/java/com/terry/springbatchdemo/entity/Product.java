package com.terry.springbatchdemo.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@SequenceGenerator(
        name = "productIdxSequenceGenerator"
        , sequenceName = "PRODUCT_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="PRODUCT")
@Getter
@EqualsAndHashCode
@ToString
@Access(AccessType.FIELD)
public class Product {

    private Long idx;

    @Column(name="PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name="PRODUCT_PRICE", nullable = false)
    private Integer productPrice;

    @OneToMany
    private List<ShoppingItem> shoppingItemList;

    @Builder
    public Product(String productName, Integer productPrice) {
        this.productName = productName;
        this.productPrice = productPrice;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productIdxSequenceGenerator")
    @Column(name="IDX")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }

    private void setIdx(Long idx) {
        this.idx = idx;
    }

    public void addShoppingItem(ShoppingItem shoppingItem) {
        this.shoppingItemList.add(shoppingItem);
    }

    public void deleteShoppingItem(ShoppingItem shoppingItem) {
        shoppingItemList.remove(shoppingItem);
    }
}
