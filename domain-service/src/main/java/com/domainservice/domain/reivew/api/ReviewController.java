package com.domainservice.domain.reivew.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.reivew.constant.ReviewConstant;
import com.domainservice.domain.reivew.docs.CreateReviewApiDocs;
import com.domainservice.domain.reivew.model.dto.request.ReviewRequest;
import com.domainservice.domain.reivew.model.dto.response.ReviewResponse;
import com.domainservice.domain.reivew.service.ReviewService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 해당 상품을 판매한 판매자의 리뷰.
 */
@Tag(name = "Review", description = "리뷰 API")
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
	@CreateReviewApiDocs
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<ReviewResponse> createReview(
		@Valid @RequestBody ReviewRequest request,
		@RequestHeader(value = "X-REQUEST-ID") String userId
	) {
		ReviewResponse response = reviewService.createReview(request, userId);

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
		@Valid @RequestBody ReviewRequest request,
		@RequestHeader(value = "X-REQUEST-ID") String userId
	) {
		ReviewResponse response = reviewService.updateReview(reviewId, request, userId);
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
		@RequestHeader(value = "X-REQUEST-ID") String userId,
		@RequestParam Integer pageNumber
	) {
		List<ReviewResponse> responseList = reviewService.getReviewListByUserId(userId, pageNumber);
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
