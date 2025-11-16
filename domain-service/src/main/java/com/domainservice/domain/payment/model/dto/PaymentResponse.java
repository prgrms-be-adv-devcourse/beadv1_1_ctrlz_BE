package com.domainservice.domain.payment.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.model.enums.PayType;
import com.domainservice.domain.payment.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

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
            paymentEntity.getOrder().getId(),
            paymentEntity.getAmount(),
            paymentEntity.getDepositUsedAmount(),
            paymentEntity.getTossChargedAmount(),
            paymentEntity.getCurrency(),
            paymentEntity.getPayType(),
            paymentEntity.getStatus(),
            paymentEntity.getApprovedAt());
    }
}
