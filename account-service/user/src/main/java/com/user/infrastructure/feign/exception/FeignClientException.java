package com.user.infrastructure.feign.exception;

import lombok.Getter;


@Getter
public class FeignClientException extends RuntimeException {

	private final String message;

	public FeignClientException(String message) {
		this.message = message;
	}

	public FeignClientException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}
}
