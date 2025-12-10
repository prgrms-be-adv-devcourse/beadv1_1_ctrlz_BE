package com.domainservice.domain.post.kafka.mapper;

import java.util.List;

import com.common.event.productPost.EventType;
import com.common.event.productPost.ProductPostDeletedEvent;
import com.common.event.productPost.ProductPostUpsertedEvent;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public class KafkaEventMapper {

	// ProductPost → ProductPostUpsertedEvent 변환
	public static ProductPostUpsertedEvent toUpsertEvent(ProductPost productPost, String categoryName, EventType eventType) {

		List<String> tagNames = productPost.getTagNames();

		return ProductPostUpsertedEvent.builder()
			.id(productPost.getId())
			.userId(productPost.getUserId())
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

	public static ProductPostDeletedEvent toDeleteEvent(String postId) {
		return new ProductPostDeletedEvent(postId, EventType.DELETE);
	}
}
