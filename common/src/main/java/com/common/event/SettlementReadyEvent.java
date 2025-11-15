package com.common.event;

import java.math.BigDecimal;

public record SettlementReadyEvent(
	String userId,
	BigDecimal netAmount,
	String settlementId
) {
}