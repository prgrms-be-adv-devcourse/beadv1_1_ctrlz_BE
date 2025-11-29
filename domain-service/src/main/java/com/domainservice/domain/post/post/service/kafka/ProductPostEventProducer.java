package com.domainservice.domain.post.post.service.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.common.event.productPost.ProductPostEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostEventProducer {

	@Value("${custom.product-post.topic.event}")
	private String topicName;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	/**
	 * 상품 이벤트를 Kafka로 발행
	 */
	public void sendEvent(ProductPostEvent productPostEvent) {

		CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, productPostEvent);

		future.whenComplete((result, ex) -> {
			if (ex == null) {
				log.info("✅ 이벤트 전송 성공 - ID: {}, Type: {}",
					productPostEvent.getId(), productPostEvent.getEventType());
			} else {
				log.error("❌ 이벤트 전송 실패 - ID: {}, Type: {}",
					productPostEvent.getId(), productPostEvent.getEventType(), ex);
			}
		});

	}
}