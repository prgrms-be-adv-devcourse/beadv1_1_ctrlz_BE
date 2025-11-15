package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SettlementExceptionCode {

	SETTLEMENT_NOT_FOUND(400, "정산이 존재하지않습니다.");

	private final int code;
	private final String message;

	public String addIdInMessage(String id) {
		return this.message + "id: " + id;
	}

}
