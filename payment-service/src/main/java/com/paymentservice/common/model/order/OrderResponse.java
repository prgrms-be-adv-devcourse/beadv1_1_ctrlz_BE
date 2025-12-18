package com.paymentservice.common.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
