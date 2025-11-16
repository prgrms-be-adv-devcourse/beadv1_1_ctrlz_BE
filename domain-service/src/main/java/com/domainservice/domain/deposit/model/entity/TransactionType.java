package com.domainservice.domain.deposit.model.entity;

public enum TransactionType {
	CHARGE,   // 충전
	PURCHASE, // 구매
	WITHDRAW, // 출금
	REFUND,    // 환불
	SETTLEMENT,
	SETTLEMENT_FAIL
}