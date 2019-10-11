package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    // @Query("select sc from ShoppingCart sc join fetch sc.shoppingItemList si  where sc.idx = :shoppingCartIdx ")
    @EntityGraph(attributePaths = {"shoppingItemList.product"})
    @Query("select sc from ShoppingCart sc where sc.idx = :shoppingCartIdx ")
    Optional<ShoppingCart> fullJoinFindById(@Param("shoppingCartIdx") Long idx);
}
