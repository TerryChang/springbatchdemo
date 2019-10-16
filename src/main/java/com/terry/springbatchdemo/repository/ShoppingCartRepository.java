package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    // @Query를 사용하여 jpql로 데이터를 조회를 하는 과정에서 @ManyToOne의 fetch mode를 EAGER로 설정해서 별다른 추가 없이 바로 조회하도록 하게 할 경우
    // ManyToOne 에 해당하는 필드에 대한 검색 쿼리가 별도로 한번 실행된다
    // EntityManager의 find 메소드를 이용하여 key 값만 넘겨주면서 조회할 경우엔 별도 쿼리가 실행되지 않고 바로 inner join 형태로 조회하지만
    // jpql일 경우엔 별도 쿼리가 실행되면서 조회된다
    // 즉 jpql을 사용할 경우 fetch mode 가 EAGER인 필드에 대해서는 그걸 조회하기 위한 별도 쿼리가 실행하게 된다
    // 원래 이것에 대해서도 정해진 정책이 있는데 ManyToOne에 대한 조회시 별도 쿼리로 실행할 것인지, 아니면 join 형태로 할 것인지를 정하는 정책이 있다
    // 해당 컬럼에 @Fetch(FetchMode.JOIN) 이라고 하면 JOIN 형태로 조회하게 되고 @Fetch(FetchMode.SELECT) 로 하면 별도 쿼리를 실행하여 조회하게 된다
    // 그러나 이 @Fetch 어노테이션도 EntityManager의 find 메소드를 이용할때나 적용되는거지 jpql 로 조회할땐 이게 적용이 되지 않는다(@Fetch(FetchMode.SELECT)  로 동작, 이게 기본값임)
    // 그래서 jpql 사용시엔 ManyToOne의 fetch model를 LAZY 로 설정하고 jpql에서 join을 사용하는것이 낫다
    // 상황에 따라 EAGER, LAZY 이렇게 할 수는 없기 때문에 그냥 LAZY로 기본으로 설정하면 좋을듯 하다

    // @EntityGraph를 사용할 경우 내부적으로 left outer join을 사용하게 된다
    // Set 인터페이스를 사용하면 내부적으로 inner join을 사용하지 않을까 해서 바꾸어보았지만..
    // query 는 left outer join 을 하기 때문에 검색결과가 중복되어 나오는 것은 변함이 없다
    // 단지 Set 이기 때문에 DB 결과를 엔티티 객체로 변환하는 과정에서 중복된 것은 1개만 들어가는 식으로 구현될 뿐이다

    // 위와 같은 이유들로 인해 user 부분을 LAZY로 수정하고 jpql에서 join을 하는 것으로 바꾼다
    // @EntityGraph 는 사용방법은 알아두는 차원에서 지우지는 않고 주석처리 했다

    // @EntityGraph(attributePaths = {"user", "shoppingItemSet.product"}, type = EntityGraph.EntityGraphType.LOAD)
    // @Query("select sc from ShoppingCart sc where sc.idx = :shoppingCartIdx ")
    // inner join만 사용하는 것으로는 필드가 별도 엔티티 객체인 것까지 같이 조회하진 않는다. 다만 해당 엔티티 객체의 key값만 조회할 뿐이다
    // fetch join을 collection 계열 조회를 해야 할 때만 사용한다고 생각하고 있다가 다시 내용을 보고 수정했다(엔티티의 멤버변수가 엔티티 일 경우 그 엔티티를 조회할때도 fetch join 을 사용한다)
    @Query("select sc from ShoppingCart sc inner join fetch sc.user u inner join fetch sc.shoppingItemSet ss inner join fetch ss.product p where sc.idx = :shoppingCartIdx order by sc.idx, ss.idx")
    Optional<ShoppingCart> userInnerJoinFindById(@Param("shoppingCartIdx") Long idx);

    @EntityGraph(attributePaths = {"user", "shoppingItemSet.product"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("select sc from ShoppingCart sc where sc.idx = :shoppingCartIdx ")
    Optional<ShoppingCart> useEntityGraphFindById(@Param("shoppingCartIdx") Long idx);

    @Query("select sc from ShoppingCart sc where sc.idx = :shoppingCartIdx ")
    Optional<ShoppingCart> onlyQueryFindById(@Param("shoppingCartIdx") Long idx);

}
