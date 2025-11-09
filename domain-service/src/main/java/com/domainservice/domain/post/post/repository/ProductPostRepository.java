package com.domainservice.domain.post.post.repository;

import com.domainservice.domain.post.post.model.entity.ProductPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductPostRepository extends JpaRepository<ProductPost, String>,
        JpaSpecificationExecutor<ProductPost> {
}