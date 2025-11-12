package com.accountapplication.user.infrastructure.kafka;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

import com.user.application.adapter.event.CartCreatedEvent;
import com.user.application.adapter.event.DepositCreatedEvent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
@Disabled("kafka 도입 시 다시 테스트 합니다.")
// @ActiveProfiles("test")
@Getter
@Slf4j
// @Component
@KafkaListener(
	topics = {"${custom.cart.topic.command}", "${custom.deposit.topic.command}"}
)
public class TestKafkaConsumer {

	private final List<Object> testStore = new ArrayList<>();

	@KafkaHandler
	public void handler(@Payload CartCreatedEvent event) {
		log.info("event.testId() = {}", event.id());
		testStore.add(event);
	}

	@KafkaHandler
	public void handler(@Payload DepositCreatedEvent event) {
		log.info("event.testId() = {}", event.id());
		testStore.add(event);
	}

}
