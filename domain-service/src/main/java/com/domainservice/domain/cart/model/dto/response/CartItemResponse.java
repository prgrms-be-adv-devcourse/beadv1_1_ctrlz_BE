package com.domainservice.domain.cart.model.dto.response;

/**
 * 장바구니 아이템 조회 응답 dto
 */
public record CartItemResponse(
	String title,
	String name,
	int totalPrice,
	int quantity,
	boolean isSelected
) {
}
