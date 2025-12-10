package com.domainservice.domain.search.exception;

import com.common.exception.CustomException;
import com.common.exception.vo.ProductPostExceptionCode;

import lombok.Getter;

/**
 * 엘라스틱서치 관련 예외
 */
@Getter
public class ElasticSearchException extends CustomException {

	private final int code;

	public ElasticSearchException(ProductPostExceptionCode exceptionCode) {
		super(exceptionCode.getMessage());
		this.code = exceptionCode.getCode();
	}

}