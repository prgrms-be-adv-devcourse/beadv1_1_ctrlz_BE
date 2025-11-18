package com.domainservice.domain.post.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.domainservice.domain.post.post.model.entity.ProductPost;

public interface ProductPostRepository extends JpaRepository<ProductPost, String>,
	JpaSpecificationExecutor<ProductPost> {

	// Initializer 전용 메서드
	@Query("SELECT p.id FROM ProductPost p WHERE p.userId <> :userId AND p.deleteStatus = 'N'")
	List<String> findAllIdsExceptOwner(@Param("userId") String userId);

}