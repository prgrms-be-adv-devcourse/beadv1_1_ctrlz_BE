package com.aiservice.infrastructure.kafka;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.aiservice.application.RagService;
import com.aiservice.application.command.CreateProductVectorCommand;
import com.aiservice.domain.event.ProductPostDeletedEvent;
import com.aiservice.domain.event.ProductPostUpsertedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("prod")
@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${custom.product-post.topic.event}", groupId = "${spring.kafka.consumer.group-id}")
public class ProductCommandEventListener {

	private final RagService<CreateProductVectorCommand> ragService;

	@KafkaHandler
	public void handleProductUpsert(@Payload ProductPostUpsertedEvent event) {
		log.info("ProductPostUpsertedEvent 수신: {}", event);
		CreateProductVectorCommand command = new CreateProductVectorCommand(
				event.id(),
				event.title(),
				event.name(),
				event.categoryName(),
				event.status(),
				event.price().intValue(),
				event.description(),
				event.tags());

		String documentId = ragService.uploadData(command);
		log.info("vector DB 저장 성공: {}", documentId);
	}

	@KafkaHandler
	public void handleProductDelete(@Payload ProductPostDeletedEvent event) {
		ragService.deleteData(event.postId());
		log.info("vector DB 삭제 성공: {}", event.postId());
	}
}
