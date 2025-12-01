package com.domainservice.domain.search.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import com.common.event.productPost.ProductPostUpsertEvent;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;

public class SearchMapper {

	// 검색 파라미터들을 검색 요청 DTO로 변환
	public static ProductPostSearchRequest toProductPostSearchRequest(
		String q, String category, Long minPrice, Long maxPrice, String tags, String sort) {

		return new ProductPostSearchRequest(
			q, category, minPrice, maxPrice, parseTagList(tags), sort);
	}

	public static List<ProductPostSearchResponse> toProductPostSearchResponseList(
		SearchHits<ProductPostDocumentEntity> searchHits
	) {
		return searchHits.getSearchHits()
			.stream()
			.map(SearchHit::getContent)
			.map(ProductPostMapper::toProductPostSearchResponse)
			.toList();
	}

	public static ProductPostDocumentEntity toProductPostDocumentEntity(ProductPostUpsertEvent event) {
		return new ProductPostDocumentEntity(
			event.id(), event.name(), event.title(), event.description(), event.tags(),
			event.categoryName(), event.price(), event.likedCount(), event.viewCount(),
			event.status(), event.tradeStatus(), event.deleteStatus(), event.createdAt()
		);
	}

	// 태그 파싱 : 아이폰,중고 -> [아이폰, 중고]
	private static List<String> parseTagList(String tags) {
		if (tags == null || tags.isEmpty()) {
			return new ArrayList<>();
		}

		return Arrays.stream(tags.split(","))
			.map(String::trim)
			.toList();
	}

}
