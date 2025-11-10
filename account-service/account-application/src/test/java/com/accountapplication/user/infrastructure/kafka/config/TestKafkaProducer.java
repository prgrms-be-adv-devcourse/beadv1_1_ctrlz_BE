package com.accountapplication.user.infrastructure.kafka.config;

import org.junit.jupiter.api.Disabled;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Disabled("kafka 도입 시 다시 테스트 합니다.")
@Slf4j
@RequiredArgsConstructor
// @Profile("test")
// @Component
public class TestKafkaProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public <T> void send(String topic, T event) {
		kafkaTemplate.send(topic, event);
		log.info(String.format("Sending message to %s", topic));
	}
}