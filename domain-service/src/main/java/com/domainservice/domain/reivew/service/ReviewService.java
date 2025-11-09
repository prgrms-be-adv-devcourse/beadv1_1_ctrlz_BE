package com.domainservice.domain.reivew.service;

import java.util.List;

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

	@Transactional
	public ReviewResponse createReview(ReviewRequest request) {
		Review newReview = Review.builder()
			.contents(request.contents())
			.userRating(request.userRating())
			.productRating(request.productRating())
			.build();

		Review savedReview = reviewRepository.save(newReview);

		return ReviewResponse.from(savedReview);

	}

	@Transactional
	public ReviewResponse updateReview(String reviewId, ReviewRequest request) {
		//TODO: 오류 처리 로직을 한번 볼 필요가 있음, 임시로 IllegalArgumentException으로 작성
		Review findReview = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 리뷰가 없습니다."));

		findReview.updateReview(request.contents(), request.userRating(), request.productRating());

		return ReviewResponse.from(findReview);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> getReviewListById(String userId) {
		return reviewRepository.findAllByUserId(userId).stream()
			.map(ReviewResponse::from)
			.toList();
	}

	@Transactional
	public ReviewResponse getReviewByProductPostId(String productPostId) {
		Review findrReview = reviewRepository.findByProductPostId(productPostId)
			.orElseGet(null);

		return findrReview == null
			? null
			: ReviewResponse.from(findrReview);
	}
}
