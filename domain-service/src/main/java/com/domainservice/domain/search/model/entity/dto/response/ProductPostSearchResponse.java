package com.domainservice.domain.search.model.entity.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * Elasticsearch 검색 결과 전용 응답 DTO
 */

@Builder
public record ProductPostSearchResponse(
	String id,
	String title,
	Long price,
	Long likedCount,
	Long viewCount,
	String tradeStatus,
	String primaryImageUrl,
	LocalDateTime updatedAt
) {
}