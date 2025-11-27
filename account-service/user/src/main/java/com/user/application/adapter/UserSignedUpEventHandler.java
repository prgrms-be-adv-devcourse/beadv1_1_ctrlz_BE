package com.user.application.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.user.application.adapter.dto.CartCreateCommand;
import com.user.application.adapter.dto.DepositCreateCommand;
import com.user.application.adapter.vo.CommandType;
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

	@Value("${custom.deposit.topic.command}")
	private String depositCommandTopic;

	private final ExternalEventPersistentPort externalEventPersistentPort;
	private final OutboundEventPublisher kafkaEventPublisher;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void saveExternalEvent(UserSignedUpEvent event) {
		externalEventPersistentPort.save(
			event.userId(),
			event.eventType().name(),
			CommandType.CART_COMMAND.name(),
			CommandType.DEPOSIT_COMMAND.name()
		);

		log.info("이벤트 저장 완료: {}, {}, {}, {}",
			event.userId(),
			event.eventType(),
			CommandType.CART_COMMAND,
			CommandType.DEPOSIT_COMMAND
		);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishCartCommand(UserSignedUpEvent event) {
		CartCreateCommand cartCreateCommand = new CartCreateCommand(event.userId());
		log.info("cart command published kafka 전송: {}", event.userId());
		try {
			kafkaEventPublisher.publish(cartCommandTopic, cartCreateCommand);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		externalEventPersistentPort.completePublish(event.userId(), event.eventType().name(), CommandType.CART_COMMAND.name());
		log.info("cart command published 상태 변경: {}", event.userId());
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishDepositCommand(UserSignedUpEvent event) {
		DepositCreateCommand depositCreateCommand = new DepositCreateCommand(event.userId());
		log.info("deposit command published 상태 변경: {}", event.userId());
		try {
			kafkaEventPublisher.publish(depositCommandTopic, depositCreateCommand);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		externalEventPersistentPort.completePublish(event.userId(), event.eventType().name(), CommandType.DEPOSIT_COMMAND.name());
		log.info("deposit command published 상태 변경: {}", event.userId());
	}
}
