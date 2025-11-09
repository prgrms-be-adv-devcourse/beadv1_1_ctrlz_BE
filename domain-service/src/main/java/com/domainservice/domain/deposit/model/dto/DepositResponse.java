package com.domainservice.domain.deposit.model.dto;

// DepositResponse.java

public record DepositResponse(
	String depositId,
	int balance,
	String message
) {
}