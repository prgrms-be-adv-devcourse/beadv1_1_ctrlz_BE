package com.paymentservice.common.configuration.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.common.event.SettlementCreatedEvent;

@Configuration
public class KafkaProducerConfiguration {

	@Bean
	public KafkaTemplate<String, SettlementCreatedEvent> settlementKafkaTemplate(
		ProducerFactory<String, SettlementCreatedEvent> producerFactory
	) {
		KafkaTemplate<String, SettlementCreatedEvent> kafkaTemplate = 
			new KafkaTemplate<>(producerFactory);
		// Micrometer Observation을 통한 트레이스 전파 활성화
		kafkaTemplate.setObservationEnabled(true);
		return kafkaTemplate;
	}
}
