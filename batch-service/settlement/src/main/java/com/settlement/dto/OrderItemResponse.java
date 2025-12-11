package com.settlement.common.model.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        String orderItemId,
        BigDecimal priceSnapshot,
        String orderItemStatus) {
}
