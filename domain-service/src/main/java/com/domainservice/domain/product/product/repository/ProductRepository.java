package com.domainservice.domain.product.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.domainservice.domain.product.product.model.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
