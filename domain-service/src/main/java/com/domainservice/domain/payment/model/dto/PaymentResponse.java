package com.domainservice.domain.payment.model.dto;

import java.time.LocalDateTime;

import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.model.enums.PayType;

public record PaymentResponse(
    String userId,
    String paymentKey,
    String orderId,
    int amount,
    int depositUsedAmount,
    int tossChargedAmount,
    String currenty,
    PayType payType,
    String status,
    String failureReason,
    LocalDateTime approvedAt) {
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
            paymentEntity.getFailureReason(),
            paymentEntity.getApprovedAt());
    }

    /** 결제 성공 여부 반환 */
    public boolean isSuccess() {
        return "DONE".equalsIgnoreCase(this.status);
    }
}
