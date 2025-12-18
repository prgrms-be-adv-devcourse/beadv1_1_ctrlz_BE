package com.domainservice.domain.review.exception;

import com.common.exception.vo.ReviewExceptionCode;

public class ReviewNotFoundException extends ReviewException{
	public static final ReviewException EXCEPTION = new ReviewNotFoundException();

	private ReviewNotFoundException() {
		super(ReviewExceptionCode.REVIEW_NOT_FOUND);
	}
}
