package com.paymentservice.deposit.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TossChargeResponse(
    String userId,
    BigDecimal balance,
    String paymentKey,
    String currency,
    OffsetDateTime approvedAt
) {
}
