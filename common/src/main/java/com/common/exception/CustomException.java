package com.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final String message;

	public CustomException(String message) {
		this.message = message;
	}

	public CustomException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}
}
