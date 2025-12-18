package com.aiservice.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ProductPostUpsertedEvent(
		String id,
		String userId,
		String name,
		String title,
		String description,
		List<String> tags,
		String categoryName,
		Long price,
		Long likedCount,
		Long viewCount,
		String status,
		String tradeStatus,
		String deleteStatus,
		String primaryImageUrl,

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		LocalDateTime createdAt,

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		LocalDateTime updatedAt,

		EventType eventType
) {
}