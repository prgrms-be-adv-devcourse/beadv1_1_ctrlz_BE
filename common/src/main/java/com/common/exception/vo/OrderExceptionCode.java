package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderExceptionCode {

	PRODUCT_NOT_AVAILABLE(400, "현재 판매중이 아닌 상품으로 구매할 수 없습니다."),
	ORDER_NOT_FOUND(404, "주문을 찾을 수 없습니다"),
	ORDERITEM_NOT_FOUND(404, "주문을 찾을 수 없습니다"),
	ORDER_UNAUTHORIZED(403, "주문 취소 권한이 없습니다"),
	ORDER_CANNOT_CANCEL(400, "취소할 수 없는 주문 상태입니다"),
	ORDER_CANNOT_CONFIRM(400, "구매확정 할 수 없는 주문 상태입니다"),

	;

	private final int code;
	private final String message;

	public String addIdInMessage(String id) {
		return this.message + "id: " + id;
	}

}
