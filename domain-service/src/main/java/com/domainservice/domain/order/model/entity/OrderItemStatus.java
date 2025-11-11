package com.domainservice.domain.order.model.entity;

public enum OrderItemStatus {
	PAYMENT_PENDING, // 주문
	CANCELLED,      // 결제 이전 취소
	PAYMENT_COMPLETED,        // 결제 완료
	PAYMENT_FAILED,     // 결제실패
	REFUND_AFTER_PAYMENT,
	REFUND_BEFORE_SETTLEMENT,
	PURCHASE_CONFIRMED,      // 구매확정 (정산 대기)
	SETTLED,        // 정산 완료
}