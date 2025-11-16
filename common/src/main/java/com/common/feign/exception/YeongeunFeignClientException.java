package com.common.feign.exception;

import org.springframework.http.HttpStatus;

public abstract class YeongeunFeignClientException extends Exception {

	private final HttpStatus status;
	private final String message;

	protected YeongeunFeignClientException(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

	public static class BadRequest extends YeongeunFeignClientException {
		public BadRequest(String message) {
			super(HttpStatus.BAD_REQUEST, message);
		}
	}

	public static class Unauthorized extends YeongeunFeignClientException {
		public Unauthorized(String message) {
			super(HttpStatus.UNAUTHORIZED, message);
		}
	}

	public static class NotFound extends YeongeunFeignClientException {
		public NotFound(String message) {
			super(HttpStatus.NOT_FOUND, message);
		}
	}

	public static class InternalServerError extends YeongeunFeignClientException {
		public InternalServerError(String message) {
			super(HttpStatus.INTERNAL_SERVER_ERROR, message);
		}
	}

	public static class TooManyRequests extends YeongeunFeignClientException {
		public TooManyRequests(String message) {
			super(HttpStatus.TOO_MANY_REQUESTS, message);
		}
	}

	public static class CustomError extends YeongeunFeignClientException {
		public CustomError(HttpStatus status, String message) {
			super(status, message);
		}
	}


}