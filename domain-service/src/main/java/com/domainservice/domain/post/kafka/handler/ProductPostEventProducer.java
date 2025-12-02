package com.domainservice.domain.post.kafka.handler;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.common.event.productPost.EventType;
import com.common.event.productPost.ProductPostDeleteEvent;
import com.common.event.productPost.ProductPostUpsertEvent;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;
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
	private String topicName;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final ProductPostRepository  productPostRepository;
	private final CategoryRepository categoryRepository;

	/**
	 * Upsert(CREATE/UPDATE) 이벤트를 Kafka로 발행
	 */
	public void sendUpsertEvent(ProductPost productPost, EventType eventType) {

		ProductPostUpsertEvent event = convertToUpsertEvent(productPost, eventType);

		kafkaTemplate.send(topicName, event.id(), event);

	}

	public void sendUpsertEventById(String postId, EventType eventType) {

		ProductPost target = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		ProductPostUpsertEvent event = convertToUpsertEvent(target, eventType);

		kafkaTemplate.send(topicName, event.id(), event);

	}

	/**
	 * Delete 이벤트를 Kafka로 발행
	 */
	public void sendDeleteEvent(String postId) {

		ProductPostDeleteEvent event = new ProductPostDeleteEvent(postId, EventType.DELETE);

		kafkaTemplate.send(topicName, event.postId(), event);

	}

	// ProductPost → ProductPostUpsertEvent 변환
	private ProductPostUpsertEvent convertToUpsertEvent(ProductPost productPost, EventType eventType) {
		// TODO: Category 조회로 N+1 문제 가능성, 추후 최적화 필요
		String categoryName = categoryRepository.findById(productPost.getCategoryId())
			.map(Category::getName)
			.orElse(null);

		List<String> tagNames = productPost.getProductPostTags().stream()
			.map(ppt -> ppt.getTag().getName())
			.toList();

		return ProductPostUpsertEvent.builder()
			.id(productPost.getId())
			.name(productPost.getName())
			.title(productPost.getTitle())
			.description(productPost.getDescription())
			.tags(tagNames)
			.categoryName(categoryName)
			.price(productPost.getPrice().longValue())
			.likedCount(productPost.getLikedCount().longValue())
			.viewCount(productPost.getViewCount().longValue())
			.status(productPost.getStatus().name())
			.tradeStatus(productPost.getTradeStatus().name())
			.deleteStatus(productPost.getDeleteStatus().name())
			.createdAt(productPost.getCreatedAt())
			.eventType(eventType)
			.build();
	}
}