package com.domainservice.domain.reivew.api;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.reivew.constant.ReviewConstant;
import com.domainservice.domain.reivew.model.dto.response.ReviewResponse;
import com.domainservice.domain.reivew.model.dto.request.CreateReviewRequest;
import com.domainservice.domain.reivew.model.entity.Review;
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
		@RequestBody CreateReviewRequest request
		// TODO: 사용자 id를 받아와야함
	) {
		Review review = reviewService.createReview(request);

		return new BaseResponse<>(
			ReviewResponse.from(review),
			ReviewConstant.REVIEW_CREATED.getMessage()
		);
	}
}
