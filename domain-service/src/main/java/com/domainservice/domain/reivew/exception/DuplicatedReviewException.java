package com.domainservice.domain.reivew.exception;

import com.common.exception.vo.ReviewExceptionCode;

public class DuplicatedReviewException extends ReviewException{
	public static final ReviewException EXCEPTION = new DuplicatedReviewException();

	private DuplicatedReviewException() {
		super(ReviewExceptionCode.DUPLICATED_REVIEW);
	}
}
