package com.domainservice.domain.cart.service.Consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.domainservice.CartCreateCommand;
import com.domainservice.domain.cart.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCartEventConsumer {
	private final CartService cartService;

	@KafkaListener(
		topics = "cart-command",
		groupId = "cart-create-group"
	)
	public void consume(CartCreateCommand event) {
		log.info(" 정산 이벤트 수신: {}", event);
		cartService.addCart(event.userId());
	}
}
