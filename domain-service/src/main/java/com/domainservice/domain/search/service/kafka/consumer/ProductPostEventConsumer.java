package com.domainservice.domain.search.service.kafka.consumer;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.productPost.ProductPostDeletedEvent;
import com.common.event.productPost.ProductPostUpsertedEvent;
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.repository.ProductPostElasticRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
	topics = "${custom.product-post.topic.event}",
	groupId = "${spring.kafka.consumer.group-id}"
)
public class ProductPostEventConsumer {

	private final ProductPostElasticRepository productPostElasticRepository;

	/**
	 * Upsert(CREATE/UPDATE) 이벤트 처리
	 */
	@KafkaHandler
	public void handleUpsertEvent(@Payload ProductPostUpsertedEvent event) {
		try {

			ProductPostDocumentEntity document = SearchMapper.toDocumentEntity(event);
			productPostElasticRepository.save(document);

		} catch (Exception e) {

			log.error("Upsert 이벤트 처리 실패 - ID: {}, Type: {}", event.id(), event.eventType(), e);

		}
	}

	/**
	 * Delete 이벤트 처리
	 */
	@KafkaHandler
	public void handleDeleteEvent(@Payload ProductPostDeletedEvent event) {
		try {

			productPostElasticRepository.deleteById(event.postId());

		} catch (Exception e) {

			log.error("Delete 이벤트 처리 실패 - ID: {}", event.postId(), e);

		}
	}

}