package com.domainservice.domain.order.model.dto;

import com.domainservice.domain.order.model.entity.OrderStatus;

public record OrderStatusUpdateRequest(
    OrderStatus orderStatus,
    String paymentId
) {
}
