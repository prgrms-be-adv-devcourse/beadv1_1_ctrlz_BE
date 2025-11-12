package com.user.infrastructure.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.common.exception.CustomException;
import com.common.model.web.BaseResponse;
import com.user.infrastructure.api.dto.ApiErrorResponse;
import com.user.infrastructure.feign.exception.FeignClientException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class UserExceptionHandler {

	/**
	 * feign 클라이언트 사용 시 에러 핸들러
	 *
	 * @param e the FeignClientException
	 * @return  the api error response
	 */
	@ExceptionHandler(FeignClientException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiErrorResponse handleFeignClientException(FeignClientException e) {
		log.info("FeignClientException:{}", e.getMessage(), e);

		return ApiErrorResponse.of("알 수 없는 에러가 발생했습니다.");
	}

	/**
	 * 비즈니스 로직 상 예외 발생 시
	 *
	 * @param e the CustomException
	 * @return  the api error response
	 */
	@ExceptionHandler(CustomException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse businessLogicExceptionHandler(CustomException e) {
		log.info("BusinessLogicException => {}, class = {}", e.getMessage(), e.getStackTrace()[0]);
		return ApiErrorResponse.of(e.getMessage());
	}

	/**
	 * 메서드 파라미터 유효성 검사(Validation) 실패 시
	 *
	 * @param e the e
	 * @return  the api error response
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse constraintViolationExceptionHandler(ConstraintViolationException e) {

		log.info("ConstraintViolationException => {}", e.getMessage());
		return ApiErrorResponse.of(e);
	}

	/**
	 * requestbody 유효성 검증 예외 시
	 *
	 * @param e the MethodArgumentNotValidException
	 * @return  the api error response
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {

		log.info("MethodArgumentNotValidException => {}", e.getBindingResult());
		return ApiErrorResponse.of(HttpStatus.BAD_REQUEST, e.getBindingResult());
	}

	/**
	 * 요청 파라미터 타입 불일치 시
	 *
	 * @param e the MethodArgumentTypeMismatchException
	 * @return  the api error response
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse methodArgumentTypeMismatchExceptionHandler(
		MethodArgumentTypeMismatchException e
	) {
		log.info("methodArgumentTypeMismatchExceptionHandler => {}", e.getMessage());
		return ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "잘못된 파라미터 타입입니다.");
	}

	/**
	 * 요청 파라미터 누락 시
	 *
	 * @param e the e
	 * @return  the api error response
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse missingServletRequestParameterExceptionHandler(
		MissingServletRequestParameterException e
	) {

		log.info("MissingServletRequestParameterException => {}", e.getMessage());
		return ApiErrorResponse.of(e.getMessage());
	}

	/**
	 * 처리되지 않은 모든 예외
	 *
	 * @param e the RuntimeException
	 * @return  the base response
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	public BaseResponse<ApiErrorResponse> handleRuntimeException(RuntimeException e) {

		Throwable rootCause = e;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}

		log.error("{}", e.getStackTrace());
		log.error("Caused by: {}: {}",
			rootCause.getClass().getSimpleName(),
			rootCause.getMessage()
		);

		StackTraceElement top = e.getStackTrace()[0];
		log.error("Exception at {}.{}({}:{}): {}",
			top.getClassName(),
			top.getMethodName(),
			top.getFileName(),
			top.getLineNumber(),
			e.getMessage()
		);

		return new BaseResponse<>(
			ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 에러가 발생했습니다."), "internal server error");
	}
}
