package com.paymentservice.deposit.model.dto;

import java.math.BigDecimal;

public record TossApproveDepositRequest(String paymentKey,
                                        String orderId,
                                        BigDecimal amount
) {
    public static TossApproveDepositRequest from(DepositConfirmRequest request) {
        return new TossApproveDepositRequest(
            request.paymentKey(),
            request.orderId(),
            request.amount()
        );
    }
}