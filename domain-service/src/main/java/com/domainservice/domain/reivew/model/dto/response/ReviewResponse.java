package com.domainservice.domain.reivew.model.dto.response;

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
		Review review
		// Todo: 회원 엔티티 받아오기
	) {
		return new ReviewResponse(
			review.getId(),
			review.getUserId(),
			"회원 닉네임",
			review.getContents(),
			review.getUserRating(),
			review.getProductRating(),
			false		//TODO: 자신이 작성한 리뷰가 맞는 지 확인 필요
		);
	}
}
