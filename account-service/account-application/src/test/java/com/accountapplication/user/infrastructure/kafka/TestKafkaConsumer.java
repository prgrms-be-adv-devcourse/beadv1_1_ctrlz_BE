package com.accountapplication.user.infrastructure.kafka;

import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.user.domain.event.UserSignedUpEvent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@ActiveProfiles("test")
@Component
@KafkaListener(
	topics = {"${custom.cart.topic.command}"}
)
public class TestKafkaConsumer {

	private final List<Object> testStore = new ArrayList<>();

	@KafkaHandler
	public void handler(@Payload UserSignedUpEvent event) {
		log.info("event.testId() = {}", event.eventType());
		testStore.add(event);
	}
}
