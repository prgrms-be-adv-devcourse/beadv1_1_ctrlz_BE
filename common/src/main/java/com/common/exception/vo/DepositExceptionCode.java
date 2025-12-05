package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DepositExceptionCode {
	DEPOSIT_NOT_FOUND(404, "예치금을 찾을 수 없습니다."),
	INSUFFICIENT_BALANCE(404, "예치금 잔액이 부족합니다."),
	INVALID_AMOUNT(404, "유효하지 않은 금액입니다."),
	DEPOSIT_ALREADY_EXISTS(409, "이미 존재하는 예치금입니다."),
	DEPOSIT_FAILD(502, "충전 중 오류가 발생했습니다.");
	private final int code;
	private final String message;

	public String addIdInMessage(String id) {
		return this.message + "id: " + id;
	}
}
