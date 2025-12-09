package com.common.event;

public record PaymentCompletedEvent(
    String orderId,
    String orderStatus,
    String paymentId
) {
}
