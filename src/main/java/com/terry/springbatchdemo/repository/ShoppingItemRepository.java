package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
}
