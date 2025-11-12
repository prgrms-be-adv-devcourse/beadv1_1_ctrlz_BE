package com.domainservice.domain.payment.model.dto;

public record PaymentReadyResponse(
    String userId,
    String orderId,
    int amount,
    int depositBalance,
    String orderName) {
}
