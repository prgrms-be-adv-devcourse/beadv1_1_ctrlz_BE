package com.paymentservice.deposit.model.dto;

// DepositResponse.java

import java.math.BigDecimal;

public record DepositResponse(
	String depositId,
	BigDecimal balance,
	String message
) {
}