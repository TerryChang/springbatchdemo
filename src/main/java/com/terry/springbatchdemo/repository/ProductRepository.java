package com.terry.springbatchdemo.repository;

import com.terry.springbatchdemo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
