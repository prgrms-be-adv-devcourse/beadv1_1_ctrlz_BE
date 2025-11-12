package com.domainservice.domain.payment.model.dto;

public record PaymentConfirmRequest(
    String userId,
    String paymentKey,
    String orderId,
    int amount,
    boolean depositUsed // 예치금 사용 여부
) {
}
