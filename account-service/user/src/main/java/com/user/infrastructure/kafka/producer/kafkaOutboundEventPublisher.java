package com.user.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.user.application.port.out.OutboundEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class kafkaOutboundEventPublisher implements OutboundEventPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public <T> void publish(String topicName, T event) {
		kafkaTemplate.send(topicName, event);
		log.info("Sending message to {}", event);
	}
}
