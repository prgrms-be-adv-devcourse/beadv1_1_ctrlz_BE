package com.domainservice.domain.search.model.entity.dto.request;

import java.util.List;

import lombok.Builder;

/**
 * 통합 검색 요청 DTO
 */
@Builder
public record ProductPostSearchRequest(
	String q,                    // 검색어 (optional)
	String category,             // 카테고리 (optional)
	Long minPrice,               // 최소 가격 (default: 0)
	Long maxPrice,               // 최대 가격 (default: 999999999)
	List<String> tags,           // 태그 필터 (optional)
	String sort                  // 정렬 기준 (default: score)
) {

	public ProductPostSearchRequest {
		if (minPrice == null) minPrice = 0L;
		if (maxPrice == null) maxPrice = 999999999L;
		if (sort == null || sort.isBlank()) sort = "score";
	}

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

	// 가격 필터 존재 여부 (기본값이 아닌 경우)
	public boolean hasPriceFilter() {
		return !minPrice.equals(0L) || !maxPrice.equals(999999999L);
	}
}