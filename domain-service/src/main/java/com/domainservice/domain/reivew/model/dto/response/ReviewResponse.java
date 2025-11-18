package com.domainservice.domain.reivew.model.dto.response;

import com.domainservice.common.model.user.UserResponse;
import com.domainservice.domain.reivew.model.entity.Review;

public record ReviewResponse(
	String reviewId,
	String userId,
	String nickname,
	String contents,
	Integer userRating,
	Integer productRating,
	boolean isMine
) {

	public static ReviewResponse from(
		Review review,
		UserResponse userResponse,
		String userId
	) {
		return new ReviewResponse(
			//TODO: 현재는 FeignClient로 통신시 401에러를 받음.
			review.getId(),
			review.getUserId(),
			userResponse == null ? "" : userResponse.nickname(),
			review.getContents(),
			review.getUserRating(),
			review.getProductRating(),
			userId.equals(review.getUserId())
		);
	}
}
