package com.paymentservice.common.model.order;

import java.math.BigDecimal;

public record OrderItemResponse(
	String orderItemId,
	BigDecimal priceSnapshot,
	OrderItemStatus orderItemStatus
) {
}
