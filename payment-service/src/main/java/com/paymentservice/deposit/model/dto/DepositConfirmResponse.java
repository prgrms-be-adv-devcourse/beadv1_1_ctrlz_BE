package com.paymentservice.deposit.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepositConfirmResponse(
    String orderId,
    String userId,
    String paymentKey,
    BigDecimal amount,
    String currency,
    OffsetDateTime approvedAt
) {

    public static DepositConfirmResponse from(String orderId,String userId, String paymentKey, BigDecimal amount, String currency, OffsetDateTime approvedAt) {
        return new DepositConfirmResponse(
            orderId,
            userId,
            paymentKey,
            amount,
            currency,
            approvedAt
        );
    }
}
