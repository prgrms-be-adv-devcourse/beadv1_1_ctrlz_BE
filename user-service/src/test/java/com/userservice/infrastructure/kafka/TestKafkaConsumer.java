package com.userservice.infrastructure.kafka;

import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.userservice.infrastructure.kafka.event.CartCreatedEvent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Component
@KafkaListener(
	topics = "${custom.cart.topic.command}"
)
public class TestKafkaConsumer {

	private final List<CartCreatedEvent> testStore = new ArrayList<>();

	@KafkaHandler
	public void handler(@Payload CartCreatedEvent event) {
		log.info("event.testId() = {}", event.testId());
		testStore.add(event);
	}

}
