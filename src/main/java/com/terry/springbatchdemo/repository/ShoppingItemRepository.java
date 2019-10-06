package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.Product;
import com.terry.springbatchdemo.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    Optional<List<ShoppingItem>> findByProductEquals(Product product);

    @Query("select shoppingItem from ShoppingItem shoppingItem where shoppingItem.product.idx = :productIdx")
    Optional<List<ShoppingItem>> findByProductIdx(@Param("productIdx") Long productIdx);
}
