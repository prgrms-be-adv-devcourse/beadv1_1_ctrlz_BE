package com.common.model.web;

import com.fasterxml.jackson.annotation.JsonInclude;


import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(

	@Schema(description = "응답 데이터")
	T data,

	@Schema(description = "응답 메시지", example = "요청 성공 메세지 출력")
	String message

) {
	public BaseResponse(String message) {
		this(null, message);
	}
}