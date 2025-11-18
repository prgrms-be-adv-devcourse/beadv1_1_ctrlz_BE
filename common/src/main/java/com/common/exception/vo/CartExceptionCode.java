package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CartExceptionCode {

	CARTITEM_NOT_FOUND(404, "장바구니 아이템이 존재하지 않습니다."),
	CARTITEM_ALREADY_ADDED(404, "이미 장바구니에 추가한 상품입니다."),
	CART_NOT_FOUND(404, "장바구니가 존재하지 않습니다."),
	CART_ALREADY_EXISTS(409, "장바구니가 이미 존재합니다.");
	private final int code;
	private final String message;

	public String addIdInMessage(String id) {
		return this.message + "id: " + id;
	}
}
