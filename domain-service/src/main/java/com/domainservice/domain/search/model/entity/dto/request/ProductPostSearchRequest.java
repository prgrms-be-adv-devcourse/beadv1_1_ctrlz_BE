package com.domainservice.domain.search.model.entity.dto.request;

import java.util.List;

import lombok.Builder;

/**
 * 통합 검색 요청 DTO
 */
@Builder
public record ProductPostSearchRequest(
	String q,
	String category,
	Long minPrice,
	Long maxPrice,
	List<String> tags,
	String status,
	String tradeStatus,
	String sort
) {

	// 검색어 존재 여부
	public boolean hasQuery() {
		return q != null && !q.isBlank();
	}

	public boolean hasCategory() {
		return category != null && !category.isBlank();
	}

	public boolean hasTags() {
		return tags != null && !tags.isEmpty();
	}

	public boolean hasStatus() { return !status.equals("ALL"); }

}