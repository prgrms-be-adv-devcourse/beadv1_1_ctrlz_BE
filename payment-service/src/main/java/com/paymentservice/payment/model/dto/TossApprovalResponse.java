package com.paymentservice.payment.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.paymentservice.payment.model.enums.PayType;
import com.paymentservice.payment.model.enums.PaymentStatus;

public record TossApprovalResponse(
    String orderId,
    BigDecimal amount,
    BigDecimal depositUsedAmount,
    BigDecimal tossChargedAmount,
    String currency,
    PayType payType,
    PaymentStatus paymentStatus,
    String paymentKey,
    OffsetDateTime approvedAt
) {
}
