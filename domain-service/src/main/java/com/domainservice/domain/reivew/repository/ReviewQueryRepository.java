package com.domainservice.domain.reivew.repository;

import java.util.List;

import com.domainservice.domain.reivew.model.entity.Review;

public interface ReviewQueryRepository {

	List<Review> findPagedReviewByIdAndUserId(String id, String userId);
}
