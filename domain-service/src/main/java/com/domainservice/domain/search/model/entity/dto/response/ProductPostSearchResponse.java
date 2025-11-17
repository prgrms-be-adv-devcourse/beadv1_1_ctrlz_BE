package com.domainservice.domain.search.model.entity.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

/**
 * Elasticsearch 검색 결과 전용 응답 DTO
 */
@Builder
public record ProductPostSearchResponse(
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
	LocalDateTime createdAt
) {
}
