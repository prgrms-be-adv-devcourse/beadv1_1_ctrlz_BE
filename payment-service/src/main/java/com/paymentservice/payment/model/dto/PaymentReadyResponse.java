package com.paymentservice.payment.model.dto;

import java.math.BigDecimal;

public record PaymentReadyResponse(
    String userId,
    String orderId,
    BigDecimal amount,
    BigDecimal depositBalance,
    String orderName
) {
}