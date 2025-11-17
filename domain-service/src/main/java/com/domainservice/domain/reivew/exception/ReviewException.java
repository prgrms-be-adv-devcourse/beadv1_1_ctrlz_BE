package com.domainservice.domain.reivew.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.CustomException;
import com.common.exception.vo.ReviewExceptionCode;

import lombok.Getter;


@Getter
public abstract class ReviewException extends CustomException {

	private final HttpStatus status;
	public ReviewException(ReviewExceptionCode code) {
		super(code.getMessage());
		this.status = code.getStatus();
	}
}
