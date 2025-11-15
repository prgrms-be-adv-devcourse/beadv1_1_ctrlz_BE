package com.domainservice.domain.search.mapper;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;

public class SearchMapper {

	// util 클래스로 선언
	private SearchMapper() {}

	// 검색 파라미터들을 검색 요청 DTO로 변환
	public static ProductPostSearchRequest toProductPostSearchRequest(
		String q,
		String category,
		Long minPrice,
		Long maxPrice,
		String tags,
		String sort
	) {
		List<String> tagList = parseTags(tags);

		return ProductPostSearchRequest.builder()
			.q(q)
			.category(category)
			.minPrice(minPrice)
			.maxPrice(maxPrice)
			.tags(tagList)
			.sort(sort)
			.build();
	}

	// 태그 파싱 : 아이폰,중고 -> [아이폰, 중고]
	private static List<String> parseTags(String tags) {
		return tags != null && !tags.isBlank()
			? Arrays.stream(tags.split(","))
			.map(String::trim)
			.filter(tag -> !tag.isEmpty())
			.toList()
			: null;
	}

	public static List<ProductPostSearchResponse> toProductPostSearchResponses(
		SearchHits<ProductPostDocumentEntity> searchHits
	) {
		return searchHits.getSearchHits()
			.stream()
			.map(SearchHit::getContent)
			.map(ProductPostMapper::toProductPostSearchResponse)
			.toList();
	}

}
