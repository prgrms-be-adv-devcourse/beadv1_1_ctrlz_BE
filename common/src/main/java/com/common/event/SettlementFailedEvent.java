package com.common.event;

// 예치금서비스 → 정산서비스 (실패)
public record SettlementFailedEvent(
	String settlementId,
	String userId,
	String reason // 간단한 에러 코드/메시지
) {
}