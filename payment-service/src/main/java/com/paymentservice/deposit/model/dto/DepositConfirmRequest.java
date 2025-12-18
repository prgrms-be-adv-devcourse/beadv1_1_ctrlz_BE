package com.paymentservice.deposit.model.dto;

import java.math.BigDecimal;

public record DepositConfirmRequest(
    String orderId,
    String paymentKey,
    BigDecimal amount
) {
}
