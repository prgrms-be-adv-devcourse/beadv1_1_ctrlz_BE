package com.settlementservice.service.producer;

import java.math.BigDecimal;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.common.event.SettlementReadyEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementReadyEventProducer {

	private final KafkaTemplate<String, SettlementReadyEvent> kafkaTemplate;

	public void sendSettlementReadyEvent(String userId, BigDecimal amount, String settlementId) {
		kafkaTemplate.send("settlement-ready",
			new SettlementReadyEvent(userId, amount, settlementId));
	}
}