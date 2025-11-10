package com.userservice.infrastructure.feign.exception.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.common.exception.feign.YeongeunFeignClientException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice()
public class FeignClientControllerAdvice {

	/**
	 * 클라이언트 오류: 400 Bad Request
	 */
	@ExceptionHandler(YeongeunFeignClientException.BadRequest.class)
	public ResponseEntity<?> handleBadRequestException(YeongeunFeignClientException.BadRequest e) {
		log.debug("handleBadRequestException 호출");
		log.error("잘못된 요청 형식 오류: {}", e.getMessage());

		return null;
	}


	/**
	 * 클라이언트 오류: 400 Bad Request
	 */
	@ExceptionHandler(YeongeunFeignClientException.class)
	public ResponseEntity<?> handleOthersException(YeongeunFeignClientException.BadRequest e) {

		log.error("핸들링 되지 않은 예외들입니다.: {}", e.getMessage());
		log.error("잘못된 요청 형식 오류: {}", e.getMessage());

		return null;
	}
}
