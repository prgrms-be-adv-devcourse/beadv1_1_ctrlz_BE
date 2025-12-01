package com.domainservice.domain.search.service.kafka;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.productPost.ProductPostDeleteEvent;
import com.common.event.productPost.ProductPostUpsertEvent;
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
	public void handleUpsertEvent(@Payload ProductPostUpsertEvent event) {
		try {

			log.info("Upsert 이벤트 수신 - ID: {}, Type: {}", event.id(), event.eventType());

			upsertDocument(event);

			log.info("Upsert 이벤트 처리 완료 - ID: {}, Type: {}", event.id(), event.eventType());

		} catch (Exception e) {

			log.error("Upsert 이벤트 처리 실패 - ID: {}, Type: {}", event.id(), event.eventType(), e);
			// TODO: 재시도 로직 또는 DLQ로 전송

		}
	}

	/**
	 * Delete 이벤트 처리
	 */
	@KafkaHandler
	public void handleDeleteEvent(@Payload ProductPostDeleteEvent event) {
		try {

			log.info("Delete 이벤트 수신 - ID: {}", event.postId());

			deleteDocument(event.postId());

			log.info("Delete 이벤트 처리 완료 - ID: {}", event.postId());

		} catch (Exception e) {

			log.error("Delete 이벤트 처리 실패 - ID: {}", event.postId(), e);
			// TODO: 재시도 로직 또는 DLQ로 전송

		}
	}

	/**
	 * 알 수 없는 이벤트 처리
	 */
	@KafkaHandler(isDefault = true)
	public void handleUnknownEvent(@Payload Object event) {
		log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getName());
	}

	// Elasticsearch 문서 생성/업데이트
	private void upsertDocument(ProductPostUpsertEvent event) {
		ProductPostDocumentEntity document = new ProductPostDocumentEntity(
			event.id(),
			event.name(),
			event.title(),
			event.description(),
			event.tags(),
			event.categoryName(),
			event.price(),
			event.likedCount(),
			event.viewCount(),
			event.status(),
			event.tradeStatus(),
			event.deleteStatus(),
			event.createdAt()
		);

		productPostElasticRepository.save(document);
		log.info("Elasticsearch 문서 저장 완료 - ID: {}", event.id());
	}

	// Elasticsearch 문서 삭제
	private void deleteDocument(String postId) {
		if (productPostElasticRepository.existsById(postId)) {
			productPostElasticRepository.deleteById(postId);
			log.info("Elasticsearch 문서 삭제 완료 - ID: {}", postId);
		} else {
			log.warn("삭제할 문서가 존재하지 않음 - ID: {}", postId);
		}
	}
}