package com.user.infrastructure.kafka.producer;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 추후 사용 예정
 */
@Slf4j
@RequiredArgsConstructor
@Profile("prod")
@Component
public class KafkaProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Async("taskExecutor")
	public <T> void send(String topic, T event) {
		kafkaTemplate.send(topic, event);
		log.info(String.format("Sending message to %s", topic));
	}
}
