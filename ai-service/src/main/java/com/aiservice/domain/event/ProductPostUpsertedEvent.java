package com.aiservice.domain.event;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ProductPostUpsertedEvent(
		String id,
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

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") LocalDateTime createdAt,

		EventType eventType) {
}