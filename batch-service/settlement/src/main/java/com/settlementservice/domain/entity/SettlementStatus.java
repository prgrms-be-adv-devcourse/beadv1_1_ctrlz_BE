package com.settlementservice.domain.entity;

public enum SettlementStatus {
	PENDING,    // 생성만 된 상태, 정산 대기
	READY, // 정산 조회 후 예치금 반영 전 상태
	COMPLETED,  // 정산 완료, 예치금 반영 완료
	FAILED      // 정산 실패, 재시도 필요
}