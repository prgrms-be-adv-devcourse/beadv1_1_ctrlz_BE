package com.domainservice.domain.cart.service;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.CartCreateCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@KafkaListener(
	topics = {"${custom.cart.topic.command}"},
	containerFactory = "cartKafkaListenerContainerFactory"
)
@Component
public class UserCreatedListener {

	private final CartService cartService;

	@KafkaHandler
	public void handler(@Payload CartCreateCommand cartCreateCommand) {
		cartService.addCart(cartCreateCommand.userId());
		log.info("Cart created for user: {}", cartCreateCommand.userId());
	}
}
