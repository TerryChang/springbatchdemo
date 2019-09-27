package com.terry.springbatchdemo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name="ORDER_DETAIL")
@Getter
@EqualsAndHashCode
@Access(AccessType.FIELD)
public class OrderDetail {
    private Long idx;
    private Order order;
    private Product product;
    private int product_price;
    private int product_cnt;

    public OrderDetail() {

    }

    public OrderDetail(Long idx, Order order, Product product, int product_price, int product_cnt) {
        this.idx = idx;
        this.order = order;
        this.product = product;
        this.product_price = product_price;
        this.product_cnt = product_cnt;
    }

    @Id
    @Column(name="idx")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }
}
