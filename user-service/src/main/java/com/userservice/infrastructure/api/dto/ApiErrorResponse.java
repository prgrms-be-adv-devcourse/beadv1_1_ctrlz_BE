package com.userservice.infrastructure.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

	private final int status;
	private final String message;
	private final List<CustomFieldError> customFieldErrors;

	@Builder
	public ApiErrorResponse(int status, String message, List<CustomFieldError> customFieldErrors) {
		this.status = status;
		this.message = message;
		this.customFieldErrors = customFieldErrors;
	}

	public static ApiErrorResponse of(HttpStatus httpStatus, BindingResult bindingResult) {
		return ApiErrorResponse.builder()
			.status(httpStatus.value())
			.customFieldErrors(CustomFieldError.of(bindingResult))
			.build();
	}

	public static ApiErrorResponse of(HttpStatus httpStatus, String message) {
		return ApiErrorResponse.builder()
			.status(httpStatus.value())
			.message(message)
			.build();
	}

	public static ApiErrorResponse of(ConstraintViolationException e) {
		List<CustomFieldError> errors = e.getConstraintViolations().stream()
			.map(v -> new CustomFieldError(v.getPropertyPath().toString(),
				null,
				v.getMessage()))
			.collect(Collectors.toList());
		return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), errors);
	}

	public static ApiErrorResponse of(String message) {
		return ApiErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).message(message).build();
	}

	@Getter
	public static class CustomFieldError {

		private final String field;
		private final Object rejectedValue;
		private final String reason;

		private CustomFieldError(String field, Object rejectedValue, String reason) {
			this.field = field;
			this.rejectedValue = rejectedValue;
			this.reason = reason;
		}

		public static List<CustomFieldError> of(BindingResult bindingResult) {
			final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
			return fieldErrors.stream()
				.map(error -> new CustomFieldError(error.getField(),
					error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
					error.getDefaultMessage())
				)
				.collect(Collectors.toList());
		}
	}
}
