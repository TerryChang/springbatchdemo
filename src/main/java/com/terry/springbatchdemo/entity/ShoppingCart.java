package com.terry.springbatchdemo.entity;

import lombok.*;

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
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Access(AccessType.FIELD)
public class ShoppingCart {
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "USER_IDX", foreignKey = @ForeignKey(name = "FK_USER_IDX"), nullable = false)
    private User user;

    /**
     * ShoppingItem은 중복이 허용될 수 있다.
     * 장바구니를 생각해보면 같은 상품을 여러번 가서 해당 항목을 다시 장바구니에 등록할 수 있는 것을 생각해보면 중복이 허용되는것을 알 수 있다
     * 만약 중복을 허용하지 않을꺼면 Set 인터페이스를 사용하면 된다
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL)
    private List<ShoppingItem> shoppingItemList = new ArrayList<>();

    @Column(name = "TOTAL_PRICE")
    private Long totalPrice = 0L;

    @Builder
    public ShoppingCart(User user, List<ShoppingItem> shoppingItemList) {
        this.user = user;
        user.getShoppingCartList().add(this);
        this.shoppingItemList = shoppingItemList;
        shoppingItemList.forEach(shoppingItem -> {totalPrice += shoppingItem.getTotalPrice();});
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

    public void addShoppingItem(ShoppingItem shoppingItem) {
        // 등록하고자 하는 ShoppingItem에 설정되어 있는 ShoppingCart 객체가 현재 객체가 아니면 ShoppingItem 객체에 설정되어 있는 ShoppingCart 객채에서 등록하고자 하는 ShoppingItem 객체를 지운다
        // 왜냐면 추가하고자 하는 ShoppingItem 객체가 현재 ShoppingCart 객체에 추가될 것이기 때문에 이 작업을 진행하지 않으면 추가하고자 하는 ShoppingItem 객체가 서로 다른 ShoppingCart 객체 두 군데에 존해해지기 때문이다
        if(shoppingItem.getShoppingCart() != this) {
            shoppingItem.getShoppingCart().removeShoppingItem(shoppingItem);
            shoppingItem.setShoppingCart(this);
        }

        if(!shoppingItemList.contains(shoppingItem)) {
            shoppingItemList.add(shoppingItem);
        }
    }

    public void removeShoppingItem(ShoppingItem shoppingItem) {
        shoppingItemList.remove(shoppingItem);
    }
}
