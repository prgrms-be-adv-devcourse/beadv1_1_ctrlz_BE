package com.domainservice.domain.reivew.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.CustomException;
import com.domainservice.domain.reivew.exception.code.ReviewExceptionCode;

import lombok.Getter;

@Getter
public class ReviewException extends CustomException {

	private final HttpStatus status;

	public ReviewException(ReviewExceptionCode code) {
		super(code.getMessage());
		this.status = code.getStatus();
	}
}