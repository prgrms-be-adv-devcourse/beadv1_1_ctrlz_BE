package com.domainservice.domain.post.category.repository;


import com.domainservice.domain.post.category.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

}
