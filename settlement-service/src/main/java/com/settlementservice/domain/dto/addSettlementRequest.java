package com.settlementservice.domain.dto;

import java.math.BigDecimal;

public record addSettlementRequest(
	String orderItemId,
	String userId,
	BigDecimal amount
) {
}
