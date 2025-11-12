package com.domainservice.domain.order.model.dto;

import com.domainservice.domain.order.model.entity.OrderItemStatus;

public record OrderItemResponse(
	String orderItemId,
	Integer quantity,
	int priceSnapshot,
	int totalPrice,
	OrderItemStatus orderItemStatus
) {
}
