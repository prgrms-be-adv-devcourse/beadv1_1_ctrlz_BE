package com.domainservice.common.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.common.exception.CustomException;
import com.common.model.web.ErrorResponse;
import com.domainservice.common.configuration.feign.exception.UserClientException;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.review.exception.ReviewException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리 핸들러, 500번대 에러 발생 시 Sentry와 Slack으로 알림 전송
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final ErrorNotificationService errorNotificationService;

	@ExceptionHandler({UserClientException.class, ReviewException.class})
	public ResponseEntity<ErrorResponse> handleUserClientException(UserClientException e) {
		e.printStackTrace();
		int statusCode = e.getStatus().value();

		// sentry, slack으로 에러 내용 전송
		errorNotificationService.notifyError(e, statusCode);

		ErrorResponse response = ErrorResponse.of(statusCode, e.getMessage());
		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(ProductPostException.class)
	public ResponseEntity<ErrorResponse> handleProductPostException(ProductPostException e) {
		e.printStackTrace();
		int statusCode = e.getCode();

		// sentry, slack으로 에러 내용 전송
		errorNotificationService.notifyError(e, statusCode);

		ErrorResponse response = ErrorResponse.of(statusCode, e.getMessage());
		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
		HttpMessageNotReadableException e) {

		String message = "잘못된 요청 형식입니다.";

		Throwable cause = e.getCause();
		if (cause instanceof InvalidFormatException ife) {
			if (ife.getTargetType().isEnum()) {
				Object[] enumValues = ife.getTargetType().getEnumConstants();
				String allowedValues = Arrays.stream(enumValues)
					.map(Object::toString)
					.collect(Collectors.joining(", "));

				message = String.format(
					"%s 필드는 [%s] 중 하나여야 합니다. (입력값: %s)",
					ife.getPath().get(0).getFieldName(),
					allowedValues,
					ife.getValue()
				);
			}
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				"입력값 검증에 실패했습니다.",
				e.getBindingResult()
			));
	}

	@ExceptionHandler({NoResourceFoundException.class, DataIntegrityViolationException.class})
	public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception e) {
		String message = switch (e) {
			case NoResourceFoundException nrf -> "요청하신 API 엔드포인트를 찾을 수 없습니다.";
			case DataIntegrityViolationException div -> "이미 좋아요한 글입니다.";
			default -> "잘못된 요청입니다.";
		};

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		e.printStackTrace();
		int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

		// sentry, slack으로 에러 내용 전송
		errorNotificationService.notifyError(e, statusCode);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.of(statusCode, "서버 내부 오류가 발생했습니다."));
	}
}