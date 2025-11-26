package com.user.application.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.user.application.adapter.dto.CartCreateCommand;
import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.application.port.out.OutboundEventPublisher;
import com.user.domain.event.UserSignedUpEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserSignedUpEventHandler {

	@Value("${custom.cart.topic.command}")
	private String cartCommandTopic;

	private final ExternalEventPersistentPort externalEventPersistentPort;
	private final OutboundEventPublisher kafkaEventPublisher;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void saveExternalEvent(UserSignedUpEvent event) {
		log.info("UserSignedUpEvent: {}", event);
		externalEventPersistentPort.save(event.userId(), event.eventType());
	}


	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishKafka(UserSignedUpEvent event) {
		CartCreateCommand cartCreateCommand = new CartCreateCommand(event.userId());
		log.info("cartCommandTopic: {}", event);
		try {
			kafkaEventPublisher.publish(cartCommandTopic, cartCreateCommand);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		externalEventPersistentPort.completePublish(event.userId(), event.eventType());
		log.info("externalEventPersistentPort: {}", event);
	}

}
