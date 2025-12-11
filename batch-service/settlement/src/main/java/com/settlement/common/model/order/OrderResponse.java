package com.settlement.common.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String orderName,
        String orderId,
        String buyerId,
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        String orderStatus, // Enum 대신 String으로 받아도 됨, 매핑 피하기 위해
        List<OrderItemResponse> orderItems) {
}
