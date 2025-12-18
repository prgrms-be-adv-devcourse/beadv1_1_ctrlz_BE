package com.domainservice.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.domainservice.domain.review.model.entity.Review;

public interface ReviewQueryRepository {

	List<Review> findReviewsByUserId(String userId, Pageable pageable);
}
