package com.common.model.web;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(
	T data,
	String message
) {

	public BaseResponse(String message) {
		this(null, message);
	}
}