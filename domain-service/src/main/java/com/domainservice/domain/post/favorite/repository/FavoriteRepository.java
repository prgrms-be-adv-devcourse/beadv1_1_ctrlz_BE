package com.domainservice.domain.post.favorite.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.post.favorite.model.entity.FavoriteProduct;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public interface FavoriteRepository extends JpaRepository<FavoriteProduct, String> {

	// 특정 사용자의 특정 상품 찜하기 조회
	Optional<FavoriteProduct> findByUserIdAndProductPostId(String userId, String productPostId);

	// 사용자의 찜한 상품 목록 조회
	Page<FavoriteProduct> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

	// 찜하기 존재 여부 확인
	boolean existsByUserIdAndProductPost(String userId, ProductPost productPost);
	boolean existsByUserIdAndProductPostId(String userId, String productPostId);

}