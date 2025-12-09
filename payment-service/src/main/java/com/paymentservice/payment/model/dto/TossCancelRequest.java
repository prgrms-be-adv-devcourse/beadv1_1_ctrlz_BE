package com.paymentservice.payment.model.dto;

import java.math.BigDecimal;

import com.paymentservice.payment.model.entity.PaymentEntity;

public record TossCancelRequest(
    BigDecimal cancelAmount,
                                String cancelReason
) {
    public static TossCancelRequest from(PaymentEntity payment) {
        return new TossCancelRequest(
            payment.getTossChargedAmount(),
            "사용자 요청 환불"
        );
    }
}