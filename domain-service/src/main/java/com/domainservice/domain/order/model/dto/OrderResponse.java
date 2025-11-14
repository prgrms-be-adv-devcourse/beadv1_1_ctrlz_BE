package com.domainservice.domain.order.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.domainservice.domain.order.model.entity.OrderStatus;

public record OrderResponse(
	String orderName,
	String orderId,
	String buyerId,
	LocalDateTime orderDate,
	BigDecimal totalAmount,
	OrderStatus orderStatus,
	List<OrderItemResponse> orderItems
) {
}
