package com.domainservice.domain.post.kafka.mapper;

import java.util.List;

import com.common.event.productPost.EventType;
import com.common.event.productPost.ProductPostDeleteEvent;
import com.common.event.productPost.ProductPostUpsertEvent;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public class KafkaEventMapper {

	// ProductPost → ProductPostUpsertEvent 변환
	public static ProductPostUpsertEvent toUpsertEvent(ProductPost productPost, String categoryName, EventType eventType) {

		List<String> tagNames = productPost.getTagNames();

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

	public static ProductPostDeleteEvent toDeleteEvent(String postId) {
		return new ProductPostDeleteEvent(postId, EventType.DELETE);
	}
}
