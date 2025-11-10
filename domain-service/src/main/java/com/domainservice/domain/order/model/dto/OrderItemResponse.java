package com.domainservice.domain.order.model.dto;

public record OrderItemResponse(
	String orderItemId,
	Integer quantity,
	int priceSnapshot,
	int totalPrice
) {
}
