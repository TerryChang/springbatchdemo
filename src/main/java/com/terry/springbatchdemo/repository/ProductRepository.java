package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findProductByProductName(String productName);
}
