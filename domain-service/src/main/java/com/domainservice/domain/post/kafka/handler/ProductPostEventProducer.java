package com.domainservice.domain.post.kafka.handler;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.common.event.productPost.EventType;
import com.common.event.productPost.ProductPostDeleteEvent;
import com.common.event.productPost.ProductPostUpsertEvent;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;
import com.domainservice.domain.post.kafka.mapper.KafkaEventMapper;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostEventProducer {

	@Value("${custom.product-post.topic.event}")
	private String eventTopicName;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final ProductPostRepository productPostRepository;
	private final CategoryRepository categoryRepository;

	/**
	 * Upsert(CREATE/UPDATE) 이벤트를 Kafka로 발행
	 */
	public void sendUpsertEvent(ProductPost productPost, EventType eventType) {

		String categoryName = getCategoryName(productPost);

		ProductPostUpsertEvent event = KafkaEventMapper.toUpsertEvent(productPost, categoryName, eventType);

		kafkaTemplate.send(eventTopicName, event.id(), event);

	}

	public void sendUpsertEventById(String postId, EventType eventType) {

		ProductPost target = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		String categoryName = getCategoryName(target);

		ProductPostUpsertEvent event = KafkaEventMapper.toUpsertEvent(target, categoryName, eventType);

		kafkaTemplate.send(eventTopicName, event.id(), event);
	}

	/**
	 * Delete 이벤트를 Kafka로 발행
	 */
	public void sendDeleteEvent(String postId) {

		ProductPostDeleteEvent event = KafkaEventMapper.toDeleteEvent(postId);
		kafkaTemplate.send(eventTopicName, event.postId(), event);

	}

	private String getCategoryName(ProductPost productPost) {

		return categoryRepository.findById(productPost.getCategoryId())
			.map(Category::getName)
			.orElseThrow(() -> new ProductPostException(CATEGORY_NOT_FOUND));

	}

}