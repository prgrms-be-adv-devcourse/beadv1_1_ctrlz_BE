package com.domainservice.domain.product.brand.repository;


import com.domainservice.domain.product.brand.model.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, String> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);

}
