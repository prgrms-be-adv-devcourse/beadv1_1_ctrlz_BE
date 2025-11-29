package com.domainservice.domain.search.service.kafka;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.productPost.ProductPostEvent;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.repository.ProductPostElasticRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@KafkaListener(
	topics = "${custom.product-post.topic.event}",
	groupId = "${spring.kafka.consumer.group-id}"
)
@RequiredArgsConstructor
public class ProductPostEventConsumer {

	private final ProductPostElasticRepository productPostElasticRepository;

	/**
	 * 상품 이벤트를 수신하여 Elasticsearch에 동기화
	 */
	@KafkaHandler
	public void consume(@Payload ProductPostEvent event) {
		try {
			log.info("📥 이벤트 수신 - ID: {}, Type: {}", event.getId(), event.getEventType());

			upsertDocument(event);

			log.info("✅ 이벤트 처리 완료 - ID: {}, Type: {}", event.getId(), event.getEventType());

		} catch (Exception e) {
			log.error("❌ 이벤트 처리 실패 - ID: {}, Type: {}", event.getId(), event.getEventType(), e);

			// TODO: 재시도 로직 또는 DLQ(Dead Letter Queue)로 전송
			// 실패한 경우 ack 하지 않으면 재처리됨
		}
	}

	private void upsertDocument(ProductPostEvent event) {
		// TODO: mapper로 변환
		ProductPostDocumentEntity document = new ProductPostDocumentEntity(
			event.getId(),
			event.getName(),
			event.getTitle(),
			event.getDescription(),
			event.getTags(),
			event.getCategoryName(),
			event.getPrice(),
			event.getLikedCount(),
			event.getViewCount(),
			event.getStatus(),
			event.getTradeStatus(),
			event.getDeleteStatus(),
			event.getCreatedAt()
		);

		productPostElasticRepository.save(document);
		log.info("Elasticsearch 문서 동기화 완료 - ID: {}, TYPE: {}", event.getId(), event.getEventType());
	}
}