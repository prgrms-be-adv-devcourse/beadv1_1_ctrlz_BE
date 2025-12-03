package com.domainservice.domain.cart.service;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.CartCreateCommand;
import com.common.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@KafkaListener(
	topics = {"${custom.cart.topic.command}"},
	containerFactory = "cartKafkaListenerContainerFactory"
)
@Component
public class CartCommandListener {

	private final CartService cartService;

	@KafkaHandler
	public void handler(@Payload CartCreateCommand cartCreateCommand, Acknowledgment ack) {
		try {
			cartService.addCart(cartCreateCommand.userId());
			log.info("Cart created for user: {}", cartCreateCommand.userId());
			ack.acknowledge();
		} catch (CustomException e) {
			log.info("이미 처리된 이벤트입니다: {}", cartCreateCommand.userId());
			ack.acknowledge();
		} catch (Exception e) {
			log.error("카프카 event handler error: {}", e.getMessage(), e);
			ack.acknowledge();
			throw e;
		}
	}
}
