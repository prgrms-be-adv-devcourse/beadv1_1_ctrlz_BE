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

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	@ExceptionHandler(UserClientException.class)
	public ResponseEntity<ErrorResponse> handleUserClientException(UserClientException e) {

		e.printStackTrace();
		log.info("error={}", e);

		// 500번대 에러인 경우 Sentry에 캡처
		int statusCode = e.getStatus().value();

		if (statusCode >= 500 && statusCode < 600) {
			captureSentryException(e, statusCode);
		}

		ErrorResponse response = ErrorResponse.of(statusCode, e.getMessage());
		return ResponseEntity.status(e.getStatus()).body(response);
	}

	/**
	 * ReviewException를 상속받는 모든 예외 처리
	 */
	@ExceptionHandler(ReviewException.class)
	public ResponseEntity<ErrorResponse> handleReviewException(ReviewException e) {

		e.printStackTrace();

		int status = e.getStatus().value();

		// 500번대 에러인 경우 Sentry에 캡처
		if (status >= 500 && status < 600) {
			captureSentryException(e, status);
		}

		ErrorResponse response = ErrorResponse.of(status, e.getMessage());
		return ResponseEntity.status(status).body(response);
	}

	/**
	 * CustomException를 상속받는 모든 예외 처리
	 */
	@ExceptionHandler(ProductPostException.class)
	public ResponseEntity<ErrorResponse> handleProductPostException(ProductPostException e) {

		int statusCode = e.getCode();

		HttpStatus status = HttpStatus.valueOf(statusCode);

		// 500번대 에러인 경우 Sentry에 캡처
		if (statusCode >= 500 && statusCode < 600) {
			captureSentryException(e, statusCode);
		}

		ErrorResponse response = ErrorResponse.of(statusCode, e.getMessage());
		return ResponseEntity.status(status).body(response);
	}

	/**
	 * JSON 역직렬화 실패 처리 (400)
	 * Enum 타입 불일치, 잘못된 JSON 형식 등
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
		HttpMessageNotReadableException e) {

		String message = "잘못된 요청 형식입니다.";

		// Enum 역직렬화 실패인 경우 구체적인 메시지 제공
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

		ErrorResponse response = ErrorResponse.of(
			HttpStatus.BAD_REQUEST.value(),
			message
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	/**
	 * Validation 예외 처리 (400)
	 * 입력값이 잘못 들어온 경우
	 * 해당 입력값의 field, rejectedValue, reason을 확인할 수 있음
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
		MethodArgumentNotValidException e) {

		// ErrorResponse 생성
		ErrorResponse response = ErrorResponse.of(
			HttpStatus.BAD_REQUEST.value(),
			"입력값 검증에 실패했습니다.",
			e.getBindingResult()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoApiException(NoResourceFoundException e) {

		ErrorResponse response = ErrorResponse.of(
			HttpStatus.BAD_REQUEST.value(),
			"요청하신 API 엔드포인트를 찾을 수 없습니다."
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	/**
	 * DB 유니크 제약조건 위반 (동시 요청)
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {

		ErrorResponse response = ErrorResponse.of(
			HttpStatus.BAD_REQUEST.value(),
			"이미 좋아요한 글입니다."
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	/**
	 * CustomException를 상속받는 모든 예외 처리
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

		HttpStatus status = HttpStatus.NOT_FOUND;
		ErrorResponse response = ErrorResponse.of(status.value(), e.getMessage());

		return ResponseEntity.status(status).body(response);
	}

	/**
	 * 기타 걸러지지 않은 오류 발생 시 (500)
	 * 현재 Feign 관련 오류가 잡히는 곳(추후 수정 예정)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		e.printStackTrace();

		// 500 에러는 항상 Sentry에 캡처
		captureSentryException(e, HttpStatus.INTERNAL_SERVER_ERROR.value());

		ErrorResponse response = ErrorResponse.of(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"서버 내부 오류가 발생했습니다."
		);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(response);
	}

	/**
	 * Sentry에 예외를 캡처하는 메서드
	 */
	private void captureSentryException(Exception exception, int statusCode) {
		try {
			SentryEvent event = new SentryEvent();
			event.setLevel(SentryLevel.ERROR);
			event.setThrowable(exception);

			Message message = new Message();
			message.setMessage(exception.getMessage());
			event.setMessage(message);

			// 상태 코드를 Extra 데이터로 추가
			event.setExtra("statusCode", statusCode);
			event.setTag("http.status_code", String.valueOf(statusCode));

			Sentry.captureEvent(event);
			log.info("Sentry로 예외 전송 완료: {} (상태 코드: {})", exception.getClass().getSimpleName(), statusCode);
		} catch (Exception e) {
			log.error("Sentry로 예외 전송 중 오류 발생", e);
		}
	}

}