package com.paymentservice.deposit.model.dto;

// DepositResponse.java

import java.math.BigDecimal;

import com.paymentservice.deposit.model.entity.Deposit;

public record DepositResponse(
	String depositId,
	BigDecimal balance,
	String message
) {

	public static DepositResponse from(Deposit deposit) {
		return new DepositResponse(
			deposit.getUserId(),
			deposit.getBalance(),
			"success"
		);
	}
}