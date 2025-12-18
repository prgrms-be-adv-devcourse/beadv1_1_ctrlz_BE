package com.domainservice.domain.cart.model.dto.response;


public record CartItemResponse(
	String cartItemId,
	String title,
	String name,
	int price,
	boolean isSelected,
	String primaryImageUrl
) {
}
