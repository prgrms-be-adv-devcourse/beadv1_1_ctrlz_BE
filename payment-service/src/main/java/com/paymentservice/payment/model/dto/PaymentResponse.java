package com.paymentservice.payment.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.enums.PayType;
import com.paymentservice.payment.model.enums.PaymentStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
    String userId,
    String paymentKey,
    String orderId,
    BigDecimal amount,
    BigDecimal depositUsedAmount,
    BigDecimal tossChargedAmount,
    String currency,
    PayType payType,
    PaymentStatus status,
    OffsetDateTime approvedAt) {
    public static PaymentResponse from(PaymentEntity paymentEntity) {
        return new PaymentResponse(
            paymentEntity.getUsersId(),
            paymentEntity.getPaymentKey(),
            paymentEntity.getOrderId(),
            paymentEntity.getAmount(),
            paymentEntity.getDepositUsedAmount(),
            paymentEntity.getTossChargedAmount(),
            paymentEntity.getCurrency(),
            paymentEntity.getPayType(),
            paymentEntity.getStatus(),
            paymentEntity.getApprovedAt());
    }
}
