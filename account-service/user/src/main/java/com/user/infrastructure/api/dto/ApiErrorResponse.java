package com.user.infrastructure.api.dto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.ConstraintViolationException;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
	int status,
	String message,
	List<CustomFieldError> customFieldErrors
) {

	@Builder
	public ApiErrorResponse {
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
			.map(v -> new CustomFieldError(
				v.getPropertyPath().toString(),
				null,
				v.getMessage()
			))
			.toList();
		return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), errors);
	}

	public static ApiErrorResponse of(String message, HttpStatus httpStatus) {
		return ApiErrorResponse.builder()
			.status(httpStatus.value())
			.message(message)
			.build();
	}

	public record CustomFieldError(
		String field,
		Object rejectedValue,
		String reason
	) {

		public static List<CustomFieldError> of(BindingResult bindingResult) {
			final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
			return fieldErrors.stream()
				.map(error -> new CustomFieldError(
					error.getField(),
					error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
					error.getDefaultMessage()
				))
				.toList();
		}
	}
}