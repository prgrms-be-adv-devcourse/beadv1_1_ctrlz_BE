package com.domainservice.domain.reivew.model.dto.request;

public record ReviewRequest(

	String contents,
	Integer userRating,
	Integer productRating
) {
}
