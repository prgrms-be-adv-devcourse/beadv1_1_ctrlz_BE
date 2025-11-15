package com.common.event;

// 예치금서비스 → 정산서비스 (성공)
public record SettlementCompletedEvent(
	String settlementId,
	String userId
) {
}