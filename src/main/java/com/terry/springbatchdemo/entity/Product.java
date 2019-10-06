package com.terry.springbatchdemo.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@SequenceGenerator(
        name = "productIdxSequenceGenerator"
        , sequenceName = "PRODUCT_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="PRODUCT")
@Access(AccessType.FIELD)
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Product {

    private Long idx;

    @Column(name="PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name="PRODUCT_PRICE", nullable = false)
    private Integer productPrice;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "product")
    private List<ShoppingItem> shoppingItemList = new ArrayList<>();

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

    public void update(String productName, int productPrice) {
        this.productName = productName;
        this.productPrice = productPrice;
    }

    public void deleteShoppingItem(ShoppingItem shoppingItem) {
        shoppingItemList.remove(shoppingItem);
    }
}
