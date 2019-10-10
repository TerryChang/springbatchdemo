package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @Query("select s from ShoppingCart sc join fetch ShoppingItem si where sc.idx = :shoppingCartIdx ")
    List<Optional<ShoppingCart>> fullJoinFindById(@Param("shoppingCartIdx") Long idx);
}
