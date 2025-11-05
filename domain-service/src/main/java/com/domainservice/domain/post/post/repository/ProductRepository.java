package com.domainservice.domain.post.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public interface ProductRepository extends JpaRepository<ProductPost, Long> {
}
