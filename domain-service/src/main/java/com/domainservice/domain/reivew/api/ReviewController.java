package com.domainservice.domain.reivew.api;

import java.util.List;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.reivew.constant.ReviewConstant;
import com.domainservice.domain.reivew.model.dto.response.ReviewResponse;
import com.domainservice.domain.reivew.model.dto.request.ReviewRequest;
import com.domainservice.domain.reivew.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 해당 상품을 판매한 판매자의 리뷰.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	/**
	 * 리뷰 생성 api
	 * @param request
	 * @return 생성된 리뷰 & 응답 메시지
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<ReviewResponse> createReview(
		@RequestBody ReviewRequest request
		// TODO: 회원 정보를 가져와야함.
	) {
		ReviewResponse response = reviewService.createReview(request);

		return new BaseResponse<>(
			response,
			ReviewConstant.REVIEW_CREATED.getMessage()
		);
	}

	/**
	 * 특정 리뷰 수정(id)
	 * @param reviewId
	 * @return 특정 id의 수정된 리뷰 데이터 & 상태 메시지
	 */
	@PatchMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<ReviewResponse> getReview(
		@PathVariable String reviewId,
		@RequestBody ReviewRequest request
		//TODO: 회원 정보를 가져와야함.
	) {
		ReviewResponse response = reviewService.updateReview(reviewId, request);
		return new BaseResponse<>(
			response,
			ReviewConstant.REVIEW_UPDATED.getMessage()
		);
	}

	/**
	 * 특정 사용자의 리뷰 조회
 	 * @return
	 */
	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<List<ReviewResponse>> getReviewListById(
		String userId //TODO: 회원 정보를 가져와야함.
	) {
		List<ReviewResponse> responseList = reviewService.getReviewListById(userId);
		return new BaseResponse<>(
			responseList,
			ReviewConstant.REVIEW_FETCHED.getMessage()
		);
	}

	/**
	 * 특정 상품에 작성된 리뷰 조회 api
	 * @param productPostId
	 * @return
	 */
	@GetMapping("/{productPostId}")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<ReviewResponse> getReviewByProductPostId(
		@PathVariable String productPostId
	) {
		ReviewResponse response = reviewService.getReviewByProductPostId(productPostId);

		return new BaseResponse<>(
			response,
			ReviewConstant.REVIEW_FETCHED.getMessage()
		);
	}



}
