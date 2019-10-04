package com.terry.springbatchdemo.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@SequenceGenerator(
        name = "shoppingItemIdxSequenceGenerator"
        , sequenceName = "SHOPPING_ITEM_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="SHOPPING_ITEM")
@Getter
@EqualsAndHashCode
@ToString
@Access(AccessType.FIELD)
public class ShoppingItem {
    private Long idx;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name="PRODUCT_IDX")
    private Product product;

    @Column(name="CNT")
    private Integer cnt;

    @Column(name="TOTAL_PRICE")
    private Long totalPrice;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name="SHOPPING_CART_IDX")
    private ShoppingCart shoppingCart;

    @Builder
    public ShoppingItem(Product product, Integer cnt) {
        this.product = product;
        this.cnt = cnt;
        this.totalPrice = new Long(product.getProductPrice()) * cnt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shoppingItemIdxSequenceGenerator")
    @Column(name="IDX")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }

    private void setIdx(Long idx) {
        this.idx = idx;
    }

    public void update(Product product, Integer cnt) {
        this.product = product;
        this.cnt = cnt;
        this.totalPrice = new Long(product.getProductPrice()) * cnt;
    }

    public void setProduct(Product product) {
        this.product = product;
        product.getShoppingItemList().add(this);
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
        shoppingCart.getShoppingItemList().add(this);
    }
}
