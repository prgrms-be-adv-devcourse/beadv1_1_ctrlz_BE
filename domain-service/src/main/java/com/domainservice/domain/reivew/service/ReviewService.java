package com.domainservice.domain.reivew.service;

import java.util.List;

import com.domainservice.common.feign.client.UserFeignClient;
import com.domainservice.common.model.user.UserResponse;
import com.domainservice.domain.reivew.exception.ReviewNotFoundException;
import com.domainservice.domain.reivew.model.dto.request.ReviewRequest;
import com.domainservice.domain.reivew.model.dto.response.ReviewResponse;
import com.domainservice.domain.reivew.model.entity.Review;
import com.domainservice.domain.reivew.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserFeignClient userFeignClient;

	@Transactional
	public ReviewResponse createReview(ReviewRequest request, String userId) {
		Review newReview = Review.builder()
			.userId(userId)
			.contents(request.contents())
			.userRating(request.userRating())
			.productRating(request.productRating())
			.build();

		Review savedReview = reviewRepository.save(newReview);

		return ReviewResponse.from(savedReview, findUserById(userId), userId);

	}

	@Transactional
	public ReviewResponse updateReview(String reviewId, ReviewRequest request, String userId) {
		Review findReview = reviewRepository.findById(reviewId)
			.orElseThrow(() -> ReviewNotFoundException.EXCEPTION);

		findReview.updateReview(request.contents(), request.userRating(), request.productRating());

		return ReviewResponse.from(findReview, findUserById(userId), userId);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> getReviewListById(String userId) {
		return reviewRepository.findAllByUserId(userId).stream()
			.map(review -> ReviewResponse.from(review, findUserById(userId), userId))
			.toList();
	}

	@Transactional
	public ReviewResponse getReviewByProductPostId(String productPostId, String userId) {
		Review findReview = reviewRepository.findByProductPostId(productPostId)
			.orElseGet(null);

		return findReview == null
			? null
			: ReviewResponse.from(findReview, findUserById(userId), userId);
	}

	private UserResponse findUserById(String userId) {
		return userFeignClient.getUser(userId);
	}
}


