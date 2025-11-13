package com.domainservice.domain.order.model.dto;

import java.math.BigDecimal;

import com.domainservice.domain.order.model.entity.OrderItemStatus;

public record OrderItemResponse(
	String orderItemId,
	BigDecimal priceSnapshot,
	OrderItemStatus orderItemStatus
) {
}
