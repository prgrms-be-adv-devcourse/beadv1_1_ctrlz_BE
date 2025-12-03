package com.domainservice.domain.reivew.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.domainservice.domain.reivew.model.entity.Review;

public interface ReviewQueryRepository {

	List<Review> findReviewsByUserId(String userId, Pageable pageable);
}
