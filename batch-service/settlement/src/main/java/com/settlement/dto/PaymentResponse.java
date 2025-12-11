package com.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
                String paymentId,
                String orderItemId,
                String userId,
                BigDecimal amount,
                String status,
                LocalDateTime paidAt,
                String payType) {
}
