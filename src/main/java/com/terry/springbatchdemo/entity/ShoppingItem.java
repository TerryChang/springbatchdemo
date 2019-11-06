package com.terry.springbatchdemo.entity;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

    /**
     * @ManyToOne의 경우 Fetch 타입이 default로 FetchType.EAGER로 설정이 되어 있는데 여기서 주의할 것이 있다
     * FetchType이 EAGER로 설정되어 있는 멤버 변수 엔티티를 갖고 있는 엔티티를 조회할때 EAGER로 설정된 멤버 변수 엔티티를 같이 조회하게 되는데..
     * 이때 어떤식으로 이것을 조회할지에 대한 언급이 없다. 같이 조회하는 방법이 1개가 아니기 때문이다.
     * 우리가 일상적으로 join 해서 하겠지 하고 1개만 있을거라 생각하지만 jpa 입장에서는 이것을 join 해서 조회할수도 있고..
     * 또는 EAGER로 설정되어 있는 엔티티를 조회하는 select 문을 하나 더 만들어서 실행할수도 있기 때문이다.
     * 그래서 이것에 대해 어떤식으로 조회할지를 정해줘야 하는데
     * Hibernate에서는 @Fetch 어노테이션을 이용해서 이것을 정의할 수 있다
     * @Fetch(FetchMode.JOIN) 으로 하면 해당 엔티티와 equal join을 걸어서 같이 조회하게 되고
     * @Fetch(FetchMode.SUBSELECT) 로 하면 해당 엔티티와 조인을 걸때 equal join 방식으로 하는게 아니라 조건절에서 컬럼 in (select * from ..) 형태의 subquery 방식으로 조건을 걸어 조회하게 된다
     * @Fetch(FetchMode.SELECT) 로 하면 EAGER로 설정된 멤버 엔티티 변수를 조회하는 별도 select 문을 실행한다(N+1 쿼리 방식이다. 다만 ManyToOne에서는 대응되는 것이 Collection 변수가 아닌 1개의 객체만 저장한 변수이기 때문에 1+1의 형태가 된다)
     * 별도로 지정하지 않으면 hibernate 에서는 default로 FetchMode.SELECT로 동작한다
     *
     * 문제는 이 Fetch 방식을 정하는 것은 JPA 규격에서 정해진것이 아니기 때문에 hibernate에서는 이렇게 3가지 방법을 @Fetch 어노테이션을 이용해서 구현하지만 hibernate 가 아닌 다른 구현체일경우 @Fetch 어노테이션이 아닌 다른 어노테이션을 사용할 경우가 있고
     * 또 hibernate 같이 3개를 지원하지 않을수 있다
     * 그래서 어찌보면 @ManyToOne로 FetchMode 를 Lazy로 하고 JPQL에서 직접 join이나 fetch join을 통해서 직접 연결하는것이 낫지 싶다
     * 일단은 내용을 남겨두는 차원에서 @ManyToOne을 FetchMode를 EAGER로 사용하면서 @Fetch 어노테이션을 같이 사용하는 식으로 설정한다
     *
     * 그러나 이러한 설정도 만약 엔티티를 EntityManager 객체의 메소드를 통해 조회를 한 것이 아니라 jpql로 조회할 경우 이러한 @FetchMode 어노테이션 설정값이 무시된다
     * 그래서 @EntityGraph 를 통해서 연관되는 관계를 설정한뒤에 jpql에서 이를 활용하는 식으로 접근한다
     *
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PRODUCT_IDX", foreignKey = @ForeignKey(name="FK_SHOPPING_ITEM_PRODUCT"), nullable = false)
    private Product product;

    @Column(name="CNT")
    private Integer cnt;

    @Column(name="TOTAL_PRICE")
    private Long totalPrice;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SHOPPING_CART_IDX", foreignKey = @ForeignKey(name="FK_SHOPPING_ITEM_SHOPPING_CART"), nullable = false)
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
        if(this.shoppingCart != null) {
            this.shoppingCart.getShoppingItemSet().remove(this);
        }
        this.shoppingCart = shoppingCart;
        if(shoppingCart != null) {
            shoppingCart.addShoppingItem(this);
        }
    }
}
