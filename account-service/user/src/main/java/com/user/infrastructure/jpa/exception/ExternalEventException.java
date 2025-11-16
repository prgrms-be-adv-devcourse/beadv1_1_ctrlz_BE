package com.user.infrastructure.jpa.exception;

public class ExternalEventException extends RuntimeException {

	public ExternalEventException(String message) {
		super(message);
	}

	public ExternalEventException(String message, Throwable cause) {
		super(message, cause);
	}
}
