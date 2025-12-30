package com.domainservice.common.exception;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.common.exception.CustomException;
import com.common.model.web.ErrorResponse;
import com.domainservice.common.configuration.feign.exception.UserClientException;
import com.domainservice.common.configuration.sentry.SlackNotificationService;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.review.exception.ReviewException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final Optional<SlackNotificationService> slackNotificationService;

	@ExceptionHandler({UserClientException.class, ReviewException.class})
	public ResponseEntity<ErrorResponse> handleUserClientException(UserClientException e) {
		e.printStackTrace();
		int statusCode = e.getStatus().value();

		notifyServerError(e, statusCode);

		ErrorResponse response = ErrorResponse.of(statusCode, e.getMessage());
		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(ProductPostException.class)
	public ResponseEntity<ErrorResponse> handleProductPostException(ProductPostException e) {
		e.printStackTrace();
		int statusCode = e.getCode();

		notifyServerError(e, statusCode);

		ErrorResponse response = ErrorResponse.of(statusCode, e.getMessage());
		return ResponseEntity.status(statusCode).body(response);
	}

	// enum 필드 유효성 검사 예외
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

		ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// 유효성 검사 예외
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
		HttpStatus status = HttpStatus.NOT_FOUND;
		ErrorResponse response = ErrorResponse.of(status.value(), e.getMessage());
		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		e.printStackTrace();
		int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

		notifyServerError(e, statusCode);

		ErrorResponse response = ErrorResponse.of(statusCode, "서버 내부 오류가 발생했습니다.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	/**
	 * 500번대 에러 알림 전송
	 */
	private void notifyServerError(Exception e, int statusCode) {
		if (statusCode >= 500 && statusCode < 600) {
			captureSentryException(e, statusCode); // sentry로 해당 예외 내용 전송
			sendSlackNotification(e); // slack으로 내용 전송하여 webhook으로 수신
		}
	}

	/**
	 * Sentry에 예외 캡처
	 */
	private void captureSentryException(Exception exception, int statusCode) {
		try {
			Sentry.withScope(scope -> {
				// 태그 설정
				scope.setTag("http.status_code", String.valueOf(statusCode));
				scope.setTag("error.type", exception.getClass().getSimpleName());

				// 요청 정보 추가
				ServletRequestAttributes attributes =
					(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

				if (attributes != null) {
					HttpServletRequest request = attributes.getRequest();
					scope.setExtra("endpoint",
						String.format("%s %s", request.getMethod(), request.getRequestURL()));

					String userId = request.getHeader("X-REQUEST-ID");
					if (userId != null && !"anonymous".equals(userId)) {
						scope.setExtra("user_id", userId);
					}
				}

				Sentry.captureException(exception);
			});

			log.info("Sentry로 예외 전송 완료: {} (상태: {})",
				exception.getClass().getSimpleName(), statusCode);

		} catch (Exception e) {
			log.error("Sentry로 예외 전송 실패", e);
		}
	}

	/**
	 * Slack으로 알림 전송 (webhook)
	 */
	private void sendSlackNotification(Exception exception) {
		slackNotificationService.ifPresent(service -> {
			try {
				sendSlackWithRequest(service, exception);
			} catch (Exception e) {
				log.error("Slack 알림 전송 중 오류 발생", e);
			}
		});
	}

	// request 정보와 함께 Slack으로 알림 전송
	private void sendSlackWithRequest(SlackNotificationService service, Exception exception) {

		ServletRequestAttributes attributes =
			(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			service.sendErrorNotification(
				exception,
				request.getRequestURL().toString(),
				request.getMethod(),
				request.getHeader("X-REQUEST-ID")
			);
		} else {
			service.sendErrorNotification(exception, null, null, null);
		}

	}
}