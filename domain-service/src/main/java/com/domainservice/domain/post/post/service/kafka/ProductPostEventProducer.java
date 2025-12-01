package com.domainservice.domain.post.post.service.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.common.event.productPost.ProductPostDeleteEvent;
import com.common.event.productPost.ProductPostUpsertEvent;

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
	 * Upsert(CREATE/UPDATE) 이벤트를 Kafka로 발행
	 */
	public void sendUpsertEvent(ProductPostUpsertEvent event) {
		CompletableFuture<SendResult<String, Object>> future =
			kafkaTemplate.send(topicName, event.id(), event);

		future.whenComplete((result, ex) -> {
			if (ex == null) {
				log.info("Upsert 이벤트 전송 성공 - ID: {}, Type: {}",
					event.id(), event.eventType());
			} else {
				log.error("Upsert 이벤트 전송 실패 - ID: {}, Type: {}",
					event.id(), event.eventType(), ex);
			}
		});
	}

	/**
	 * Delete 이벤트를 Kafka로 발행
	 */
	public void sendDeleteEvent(ProductPostDeleteEvent event) {
		CompletableFuture<SendResult<String, Object>> future =
			kafkaTemplate.send(topicName, event.postId(), event);

		future.whenComplete((result, ex) -> {
			if (ex == null) {
				log.info("Delete 이벤트 전송 성공 - ID: {}, Type: {}",
					event.postId(), event.eventType());
			} else {
				log.error("Delete 이벤트 전송 실패 - ID: {}, Type: {}",
					event.postId(), event.eventType(), ex);
			}
		});
	}

}