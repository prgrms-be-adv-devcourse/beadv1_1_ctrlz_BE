package com.domainservice.domain.reivew.service;

import java.util.List;
import java.util.Optional;

import com.common.exception.CustomException;
import com.common.exception.vo.OrderExceptionCode;
import com.domainservice.common.configuration.feign.client.UserFeignClient;
import com.domainservice.common.model.user.UserResponse;
import com.domainservice.domain.order.model.dto.OrderedAt;
import com.domainservice.domain.order.repository.OrderRepository;
import com.domainservice.domain.reivew.exception.ReviewNotFoundException;
import com.domainservice.domain.reivew.model.dto.request.ReviewRequest;
import com.domainservice.domain.reivew.model.dto.response.ReviewResponse;
import com.domainservice.domain.reivew.model.entity.Review;
import com.domainservice.domain.reivew.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final OrderRepository orderRepository;
	private final UserFeignClient userFeignClient;

	@Transactional
	public ReviewResponse createReview(ReviewRequest request, String userId) {
		OrderedAt orderedAt = findOrderedAtByProductPostIdAndUserId(request.productId(), userId);

		Review newReview = Review.builder()
			.productPostId(request.productId())
			.userId(userId)
			.contents(request.contents())
			.userRating(request.userRating())
			.productRating(request.productRating())
			.build();

		Review savedReview = reviewRepository.save(newReview);

		return ReviewResponse.from(
			savedReview,
			findUserById(userId),
			orderedAt,
			userId
		);
	}

	@Transactional
	public ReviewResponse updateReview(String reviewId, ReviewRequest request, String userId) {
		Review findReview = reviewRepository.findById(reviewId)
			.orElseThrow(() -> ReviewNotFoundException.EXCEPTION);

		findReview.updateReview(request.contents(), request.userRating(), request.productRating(), userId);

		return ReviewResponse.from(
			findReview,
			findUserById(userId),
			findOrderedAtByProductPostIdAndUserId(request.productId(), userId),
			userId
		);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> getReviewListByUserId(String userId, Integer pageNumber) {
		UserResponse userResponse = findUserById(userId);

		return reviewRepository.findReviewsByUserId(userId, defaultPageable(pageNumber)).stream()
			.map(review -> ReviewResponse.from(
				review,
				userResponse,
				findOrderedAtByProductPostIdAndUserId(review.getProductPostId(), review.getUserId()),
				userId
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public ReviewResponse getReviewByProductPostId(String productPostId) {
		Optional<Review> findReview = reviewRepository.findByProductPostId(productPostId);

		if(findReview.isEmpty()) return null;

		Review review = findReview.get();
		String userId = review.getUserId();
		UserResponse userResponse = findUserById(userId);

		return ReviewResponse.from(
			review,
			userResponse,
			findOrderedAtByProductPostIdAndUserId(productPostId, userId),
			userId
		);
	}

	private UserResponse findUserById(String userId) {
		return userFeignClient.getUser(userId);
	}

	private OrderedAt findOrderedAtByProductPostIdAndUserId(String productPostId, String userId) {
		return orderRepository.findOrderedAtByBuyerIdAndProductPostId(
			productPostId,
			userId
		).orElseThrow(() -> new CustomException(OrderExceptionCode.ORDER_NOT_FOUND.getMessage()));
	}

	private Pageable defaultPageable(int pageNumber) {
		return PageRequest.of(pageNumber, 5);
	}
}


