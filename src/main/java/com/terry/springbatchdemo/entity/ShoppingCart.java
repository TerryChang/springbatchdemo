package com.terry.springbatchdemo.entity;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_IDX", foreignKey = @ForeignKey(name = "FK_SHOPPING_CART_USER"), nullable = false)
    private User user;

    /**
     * ShoppingItem은 외형적으로는 중복이 허용될것 같아 보이나 실제로는 중복이 허용되는 것이 아니다
     * 장바구니를 생각해보면 같은 상품을 여러번 가서 같은 갯수로 다시 장바구니에 등록할수는 있겠으나 그렇다고 그것 하나하나가 같은 값이라고 할 수는 없다. 왜냐면 그거 하나하나 고유 구분 키값이 존재하기 때문이다
     * 그래서 여기서는 Set 인터페이스를 사용하는 것이 더 효율적일수도 있다(순서까지 고려하면 LinkedHashSet 클래스를 사용하는게 좋다)
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL)
    @OrderBy("idx asc")
    private Set<ShoppingItem> shoppingItemSet = new LinkedHashSet<>();

    @Column(name = "TOTAL_PRICE")
    private Long totalPrice = 0L;

    @Builder
    public ShoppingCart(User user, Set<ShoppingItem> shoppingItemSet) {
        this.user = user;
        user.getShoppingCartList().add(this);
        this.shoppingItemSet = shoppingItemSet;
        shoppingItemSet.forEach(shoppingItem -> {totalPrice += shoppingItem.getTotalPrice();});
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

    public void removeShoppingItem(ShoppingItem shoppingItem) {
        // ShoppingItem의 shoppingCart 멤버변수를 null로 처리해야 하지만 이렇게 할 경우 해당 변수의 nullable을 false로 지정했기 때문에 DB 관련 작업에서 문제가 생긴다
        // ShoppingItem을 지우는 경우는 DB에서 삭제하는 경우뿐이 없기 때문에 따로 별도 처리는 하지 않았다
        /*
        if(shoppingItemSet.contains(shoppingItem)) {
            shoppingItem.setShoppingCart(null);
            shoppingItemSet.remove(shoppingItem);
        }
        */
        if(shoppingItemSet.contains(shoppingItem)) {
            shoppingItemSet.remove(shoppingItem);
        }
        shoppingItem.setShoppingCart(null);
    }
}
