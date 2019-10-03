package com.terry.springbatchdemo.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@SequenceGenerator(
        name = "shoppingCartIdxSequenceGenerator"
        , sequenceName = "SHOPPING_CART_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="SHOPPING_CART")
@Getter
@EqualsAndHashCode
@ToString
@Access(AccessType.FIELD)
public class ShoppingCart {
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "USER_IDX")
    private User user;

    @OneToMany
    private List<ShoppingItem> shoppingItemList = new ArrayList<>();
    private Long totalPrice = 0L;

    @Builder
    public ShoppingCart(List<ShoppingItem> shoppingItemList) {
        this.shoppingItemList = shoppingItemList;
        shoppingItemList.forEach(shoppingItem -> {totalPrice += shoppingItem.getTotalPriceByProduct();});
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shoppingCartIdxSequenceGenerator")
    @Column(name="IDX")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }

    private void setIdx(Long idx) {
        this.idx = idx;
    }
}
