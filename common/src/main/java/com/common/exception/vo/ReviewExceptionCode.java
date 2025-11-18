package com.common.exception.vo;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewExceptionCode {

	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰가 없습니다."),
	DUPLICATED_REVIEW(HttpStatus.BAD_REQUEST, "기존에 작성된 리뷰와 동일합니다."),
	;

	private final HttpStatus status;
	private final String message;
}
