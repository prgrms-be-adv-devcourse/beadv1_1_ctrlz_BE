package com.aiservice.exception;

public class DocumentProcessException extends RuntimeException {
	public DocumentProcessException(String message) {
		super(message);
	}

	public DocumentProcessException(String message, Throwable cause) {
		super(message, cause);
	}
}
