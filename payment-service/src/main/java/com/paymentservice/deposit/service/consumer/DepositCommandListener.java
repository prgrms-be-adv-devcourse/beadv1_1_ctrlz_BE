package com.paymentservice.deposit.service.consumer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.DepositCreateCommand;
import com.common.exception.CustomException;
import com.paymentservice.deposit.service.DepositService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@KafkaListener(
	topics = {"${custom.deposit.topic.command}"},
	containerFactory = "depositKafkaListenerContainerFactory"
)
@Component
public class DepositCommandListener {

	private final DepositService depositService;

	@KafkaHandler
	public void handler(@Payload DepositCreateCommand depositCreateCommand, Acknowledgment ack) {
		try {
			depositService.createDeposit(depositCreateCommand.userId());
			log.info("deposit created for user: {}", depositCreateCommand.userId());
			ack.acknowledge();
		} catch (CustomException e) {
			log.warn("이미 처리된 이벤트입니다: {}", depositCreateCommand.userId());
			ack.acknowledge();
		} catch (DataIntegrityViolationException e) {
			log.info("이미 처리된 이벤트입니다: {}", depositCreateCommand.userId());
			ack.acknowledge();
		} catch (Exception e) {
			log.error("카프카 event handler error: {}", e.getMessage(), e);
			ack.acknowledge();
			throw e;
		}
	}
}
