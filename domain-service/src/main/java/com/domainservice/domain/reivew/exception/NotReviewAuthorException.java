package com.domainservice.domain.reivew.exception;

import com.common.exception.vo.ReviewExceptionCode;

public class NotReviewAuthorException extends ReviewException{
	public static final ReviewException EXCEPTION = new NotReviewAuthorException();

	private NotReviewAuthorException() {
		super(ReviewExceptionCode.NOT_REVIEW_AUTHOR);
	}
}
