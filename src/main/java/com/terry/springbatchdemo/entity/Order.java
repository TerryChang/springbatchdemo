package com.terry.springbatchdemo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ORDER")
@Getter
@EqualsAndHashCode
@Access(AccessType.FIELD)
public class Order {

    private Long idx;

    @ManyToOne
    @JoinColumn(name="USER_IDX")
    private User user;

    @Column(name="TOTAL_PRICE")
    private Long totalPrice;

    @OneToMany
    @JoinColumn(name = "ORDER_IDX")
    private List<OrderDetail> orderDetailList;

    public Order() {

    }

    public Order(Long idx, User user, Long totalPrice, List<OrderDetail> orderDetailList) {
        this.idx = idx;
        this.user = user;
        this.totalPrice = totalPrice;
        this.orderDetailList = orderDetailList;
    }

    @Id
    @Column(name="idx")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }
}
