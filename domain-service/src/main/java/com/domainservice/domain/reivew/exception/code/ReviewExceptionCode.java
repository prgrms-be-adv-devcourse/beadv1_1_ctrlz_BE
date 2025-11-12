package com.domainservice.domain.reivew.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewExceptionCode {

	NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 형식의 요청입니다.")
	;

	private final HttpStatus status;
	private final String message;
}