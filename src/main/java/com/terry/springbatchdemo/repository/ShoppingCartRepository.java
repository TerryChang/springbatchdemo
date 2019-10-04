package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
}
