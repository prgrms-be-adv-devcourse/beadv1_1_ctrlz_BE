package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserExceptionCode {

	USER_NOT_FOUND(404, "존재하지 않는 유저입니다."),
	DUPLICATED_PHONE_NUMBER(409, "이미 존재하는 연락처입니다."),
	DUPLICATED_NICKNAME(409, "이미 존재하는 닉네임입니다.");

	private final int code;
	private final String message;

	public String addErrorInMessage(String error) {
		return this.message + " : " + error;
	}
}
