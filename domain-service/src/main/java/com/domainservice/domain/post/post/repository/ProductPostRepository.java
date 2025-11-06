package com.domainservice.domain.post.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public interface ProductPostRepository extends JpaRepository<ProductPost, String> {
}
