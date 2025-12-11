package com.settlement.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String orderItemId,
        BigDecimal priceSnapshot,
        String orderItemStatus) {
}
