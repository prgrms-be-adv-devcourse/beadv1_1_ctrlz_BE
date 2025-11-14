package com.user.application.adapter.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.domain.event.UserSignedUpEvent;
import com.user.infrastructure.kafka.producer.KafkaProducer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserSignedUpEventHandler {

	@Value("${custom.cart.topic.command}")
	private String cartCommandTopic;

	private final ExternalEventPersistentPort externalEventPersistentPort;
	private final KafkaProducer kafkaProducer;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void saveExternalEvent(UserSignedUpEvent event) {
		externalEventPersistentPort.save(event.userId(), event.eventType());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishKafka(UserSignedUpEvent event) {
		kafkaProducer.send(cartCommandTopic, event);
	}

}
