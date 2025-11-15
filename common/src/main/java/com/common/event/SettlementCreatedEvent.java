package com.common.event;

import java.math.BigDecimal;

public record SettlementCreatedEvent(
	String orderItemId,
	String userId,
	BigDecimal amount
) {
}