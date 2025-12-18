package com.paymentservice.payment.model.dto;

import java.math.BigDecimal;

public record TossApproveRequest(String paymentKey,
                                 String orderId,
                                 BigDecimal amount
) {
    public static TossApproveRequest from(PaymentConfirmRequest request) {
        return new TossApproveRequest(
            request.paymentKey(),
            request.orderId(),
            request.totalAmount()
        );
    }
}