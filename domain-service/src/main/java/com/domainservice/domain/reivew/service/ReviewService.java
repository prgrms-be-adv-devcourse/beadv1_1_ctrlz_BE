package com.domainservice.domain.reivew.service;

import com.domainservice.domain.reivew.model.dto.request.CreateReviewRequest;
import com.domainservice.domain.reivew.model.entity.Review;
import com.domainservice.domain.reivew.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;

	public Review createReview(CreateReviewRequest request) {

		Review newReview = Review.builder()
			.contents(request.contents())
			.userRating(request.userRating())
			.productRating(request.productRating())
			.build();

		return reviewRepository.save(newReview);

	}


}
