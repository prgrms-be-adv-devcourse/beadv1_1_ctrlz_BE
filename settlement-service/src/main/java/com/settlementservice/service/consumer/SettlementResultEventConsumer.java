package com.settlementservice.service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.common.event.SettlementCompletedEvent;
import com.common.event.SettlementFailedEvent;
import com.settlementservice.service.SettlementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementResultEventConsumer {

	private final SettlementService settlementService;

	@KafkaListener(
		topics = "settlement-completed",
		groupId = "settlement-result-group"
	)
	public void consumeCompleted(SettlementCompletedEvent event) {
		log.info("정산 완료 이벤트 수신 settlementId={}, userId={}",
			event.settlementId(), event.userId());

		settlementService.handleSettlementCompleted(event);
	}

	@KafkaListener(
		topics = "settlement-failed",
		groupId = "settlement-result-group"
	)
	public void consumeFailed(SettlementFailedEvent event) {
		log.info("정산 실패 이벤트 수신 settlementId={}, userId={}, reason={}",
			event.settlementId(), event.userId(), event.reason());

		settlementService.handleSettlementFailed(event);
	}
}
