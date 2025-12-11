package com.domainservice.common.configuration.feign.exception;

import org.springframework.http.HttpStatus;

import com.common.feign.exception.YeongeunFeignClientException;

public abstract class UserClientException extends YeongeunFeignClientException {

	public UserClientException(HttpStatus status, String message) {
		super(status, message);
	}


	public static class BadRequest extends YeongeunFeignClientException {
		public BadRequest(String message) {
			super(HttpStatus.BAD_REQUEST, message);
		}
	}

	public static class Unauthorized extends UserClientException {
		public Unauthorized(String message) {
			super(HttpStatus.UNAUTHORIZED, message);
		}
	}

	public static class NotFound extends UserClientException {
		public NotFound(String message) {
			super(HttpStatus.NOT_FOUND, message);
		}
	}

	public static class InternalServerError extends UserClientException {
		public InternalServerError(String message) {
			super(HttpStatus.INTERNAL_SERVER_ERROR, message);
		}
	}

	public static class TooManyRequests extends UserClientException {
		public TooManyRequests(String message) {
			super(HttpStatus.TOO_MANY_REQUESTS, message);
		}
	}

	public static class CustomError extends UserClientException {
		public CustomError(HttpStatus status, String message) {
			super(status, message);
		}
	}
}
