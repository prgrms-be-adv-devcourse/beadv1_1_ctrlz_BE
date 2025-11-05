package com.userservice.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public <T> void send(String topic, T event) {
		kafkaTemplate.send(topic, event);
		log.info(String.format("Sending message to %s", topic));
	}
}
