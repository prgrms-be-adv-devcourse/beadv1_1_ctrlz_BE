package com.common.exception.vo;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeignExceptionCode {


	;

	private final String message;
	private final HttpStatus status;
}
