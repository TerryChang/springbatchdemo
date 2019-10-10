package com.terry.springbatchdemo.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@SequenceGenerator(
        name = "shoppingItemIdxSequenceGenerator"
        , sequenceName = "SHOPPING_ITEM_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="SHOPPING_ITEM")
@NoArgsConstructor
@Access(AccessType.FIELD)
@Getter
@EqualsAndHashCode
@ToString
public class ShoppingItem {
    private Long idx;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name="PRODUCT_IDX", foreignKey = @ForeignKey(name="FK_PRODUCT_IDX"), nullable = false)
    private Product product;

    @Column(name="CNT")
    private Integer cnt;

    @Column(name="TOTAL_PRICE")
    private Long totalPrice;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name="SHOPPING_CART_IDX", foreignKey = @ForeignKey(name="FK_SHOPPING_CART_IDX"), nullable = false)
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
        // 기존에 설정되어 있는 ShoppingCart 객체가 있으면 기존 객체에서 현재 ShoppingItem 객체를 삭제해준다
        // 그러지 않으면 ShoppingItem 객체가 서로 다른 ShoppingCart 객체에 등록되는 상황이 오게 된다
        if(this.shoppingCart != null){
            this.shoppingCart.removeShoppingItem(this);
        }
        this.shoppingCart = shoppingCart;
        if(!shoppingCart.getShoppingItemList().contains(this)) {
            shoppingCart.getShoppingItemList().add(this);
        }
    }
}
