package com.domainservice.domain.payment.model.dto;

import java.math.BigDecimal;

public record PaymentConfirmRequest(
    String paymentKey,
    String orderId,
    BigDecimal amount,
    BigDecimal usedDepositAmount,
    BigDecimal totalAmount
) {
}
