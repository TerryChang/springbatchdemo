package com.terry.springbatchdemo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "PRODUCT")
@Getter
@EqualsAndHashCode
@Access(AccessType.FIELD)
public class Product {

    private Long idx;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "PRODUCT_PRICE")
    private int productPrice;

    public Product() {

    }

    public Product(Long idx, String productName, int productPrice) {
        this.idx = idx;
        this.productName = productName;
        this.productPrice = productPrice;
    }

    @Id
    @Column(name="IDX")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }
}

