package com.domainservice.domain.search.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import com.common.event.productPost.ProductPostUpsertedEvent;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;

public class SearchMapper {

	// 검색 파라미터들을 검색 요청 DTO로 변환
	public static ProductPostSearchRequest toSearchRequest(
		String q, String category, Long minPrice, Long maxPrice, String tags, String sort) {

		return new ProductPostSearchRequest(
			q, category, minPrice, maxPrice, parseTagList(tags), sort);
	}

	// Elasticsearch Document를 검색 응답 DTO로 변환
	public static ProductPostSearchResponse toSearchResponse(ProductPostDocumentEntity document) {
		return ProductPostSearchResponse.builder()
			.id(document.getId())
			.title(document.getTitle())
			.price(document.getPrice())
			.viewCount(document.getViewCount())
			.likedCount(document.getLikedCount())
			.tradeStatus(document.getTradeStatus())
			.primaryImageUrl(document.getPrimaryImageUrl())
			.updatedAt(document.getUpdatedAt())
			.build();
	}

	public static List<ProductPostSearchResponse> toSearchResponseList(
		SearchHits<ProductPostDocumentEntity> searchHits) {
		return searchHits.getSearchHits()
			.stream()
			.map(SearchHit::getContent)
			.map(SearchMapper::toSearchResponse)
			.toList();
	}

	public static ProductPostDocumentEntity toDocumentEntity(ProductPostUpsertedEvent event) {
		return ProductPostDocumentEntity.builder()
			.id(event.id())
			.name(event.name())
			.title(event.title())
			.price(event.price())
			.categoryName(event.categoryName())
			.description(event.description())
			.tags(event.tags())
			.likedCount(event.likedCount())
			.viewCount(event.viewCount())
			.status(event.status())
			.tradeStatus(event.tradeStatus())
			.deleteStatus(event.deleteStatus())
			.primaryImageUrl(event.primaryImageUrl())
			.createdAt(event.createdAt())
			.updatedAt(event.updatedAt())
			.build();
	}

	// 태그 파싱 : 아이폰,중고 -> [아이폰, 중고]
	private static List<String> parseTagList(String tags) {
		if (tags == null || tags.isEmpty()) {
			return new ArrayList<>();
		}

		return Arrays.stream(tags.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();
	}

}
