package com.settlementservice.service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.common.event.SettlementCreatedEvent;
import com.settlementservice.service.SettlementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseConfirmedEventConsumer {

	private final SettlementService settlementService;

	@KafkaListener(topics = "purchase-confirmed", groupId = "settlement-group")
	public void consume(SettlementCreatedEvent event) {
		log.info(" 정산 이벤트 수신: {}", event);

		settlementService.createSettlement(
			event.orderItemId(),
			event.userId(),
			event.amount()
		);
	}
}