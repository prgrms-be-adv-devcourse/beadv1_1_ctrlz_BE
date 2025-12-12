package com.domainservice.domain.reivew.model.dto.response;

import java.time.LocalDateTime;
import com.domainservice.common.model.user.UserResponse;
import com.domainservice.domain.order.model.dto.OrderedAt;
import com.domainservice.domain.reivew.model.entity.Review;

public record ReviewResponse(
	String reviewId,
	String userId,
	String nickname,
	String profileImageUrl,
	String contents,
	Integer userRating,
	Integer productRating,
	LocalDateTime orderedAt,
	boolean isMine
) {

	public static ReviewResponse from(
		Review review,
		UserResponse userResponse,
		OrderedAt orderedAt,
		String userId
	) {
		return new ReviewResponse(
			review.getId(),
			review.getUserId(),
			userResponse.nickname(),
			userResponse.profileImageUrl(),
			review.getContents(),
			review.getUserRating(),
			review.getProductRating(),
			orderedAt.date(),
			userId.equals(review.getUserId())
		);
	}
}
