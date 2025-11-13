package com.domainservice.domain.order.model.entity;

public enum OrderItemStatus {
	PAYMENT_PENDING,      // 결제 대기
	PAYMENT_COMPLETED,    // 결제 완료
	CANCELLED,            // 결제 전 취소
	REFUND_AFTER_PAYMENT, // 결제 후 환불
	PURCHASE_CONFIRMED,   // 구매 확정 (정산 대기)
	SETTLED               // 정산 완료
}