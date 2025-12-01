package com.common.event.productPost;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ProductPostUpsertEvent(
	String id,
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

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") // 밀리초 이슈 방지를 위해 .SSS 권장
	LocalDateTime createdAt,

	EventType eventType
) {
}