package com.settlementservice.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record addSettlementRequest(
	@NotBlank(message = "Order item ID is required") String orderItemId,
	@NotBlank(message = "User ID is required") String userId,
	@NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount
) {
}
