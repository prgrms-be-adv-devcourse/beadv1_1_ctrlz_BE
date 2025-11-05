package com.domainservice.domain.Order.model.entity;

public enum OrderStatus {
	PAYMENT_PENDING,    // 결제대기
	PAYMENT_COMPLETED,  // 결제완료
	PAYMENT_FAILED,     // 결제실패
	PURCHASE_CONFIRMED, // 구매확정
	REFUNDED            // 환불
}
