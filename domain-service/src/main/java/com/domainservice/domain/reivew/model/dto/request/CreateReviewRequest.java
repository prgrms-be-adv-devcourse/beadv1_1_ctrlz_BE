package com.domainservice.domain.reivew.model.dto.request;

public record CreateReviewRequest(

	String contents,
	Integer userRating,
	Integer productRating
) {
}
