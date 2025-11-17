package com.domainservice.common.configuration.feign.exception;

import org.springframework.http.HttpStatus;

import com.common.feign.exception.YeongeunFeignClientException;

public class UserClientException extends YeongeunFeignClientException {


	public UserClientException(HttpStatus status, String message) {
		super(status, message);
	}
}
