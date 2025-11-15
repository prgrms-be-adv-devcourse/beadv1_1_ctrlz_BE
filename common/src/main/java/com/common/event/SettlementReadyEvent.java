package com.common.event;

import java.math.BigDecimal;

public record SettlementReadyEvent(
	String userId,
	BigDecimal amount,
	String settlementId
) {
}