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
			review.getId(),
			review.getUserId(),
			userResponse.nickname(),
			review.getContents(),
			review.getUserRating(),
			review.getProductRating(),
			review.getUserId().equals(userId)
		);
	}
}
