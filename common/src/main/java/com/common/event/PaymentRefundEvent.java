package com.common.event;

public record PaymentRefundEvent(
    String orderId,
    String orderStatus,
    String paymentId
) {
}
