package com.domainservice.domain.order.service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.common.event.SettlementCreatedEvent;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class PurchaseConfirmedEventProducer {

	private final KafkaTemplate<String, SettlementCreatedEvent> settlementKafkaTemplate;

	public void send(SettlementCreatedEvent event) {
		settlementKafkaTemplate.send("purchase-confirmed", event);
	}
}