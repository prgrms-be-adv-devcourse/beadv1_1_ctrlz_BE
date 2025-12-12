package com.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
                String orderName,
                String orderId,
                String buyerId,
                LocalDateTime orderDate,
                BigDecimal totalAmount,
                String orderStatus,
                List<OrderItemResponse> orderItems) {
}
