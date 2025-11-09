package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserExceptionCode {

	USER_NOT_FOUND(404, "존재하지 않는 유저입니다."),
	DUPLICATED_PHONE_NUMBER(409, "이미 존재하는 연락처입니다."),
	DUPLICATED_NICKNAME(409, "이미 존재하는 닉네임입니다."),
	ALREADY_SELLER(409, "이미 판매자로 등록돼있습니다."),
	VERIFICATION_COUNT_LIMIT(404, "인증 횟수를 초과하였습니다."),
	NOT_YOUR_PHONE(400, "본인의 연락처이어야 합니다."),
	CODE_MISMATCH(400, "인증 번호가 일치하지 않습니다.");

	private final int code;
	private final String message;

	public String addErrorInMessage(String error) {
		return this.message + " : " + error;
	}
}
