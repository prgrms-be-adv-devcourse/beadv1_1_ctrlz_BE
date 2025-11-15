package com.domainservice.domain.search.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.repository.ProductPostElasticRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostElasticService {

	private final ProductPostElasticRepository productPostElasticRepository;

	/**
	 * Elasticsearch를 사용하여 상품을 검색하고 페이징된 결과를 반환합니다.
	 *
	 * @param query     검색어 (상품명, 제목, 설명, 태그 등에서 검색)
	 * @param category  카테고리명
	 * @param minPrice  최소 가격
	 * @param maxPrice  최대 가격
	 * @param tags      콤마 구분 태그 문자열 (예: "게이밍,기계식")
	 * @param pageable  페이징 정보
	 */
	public PageResponse<List<ProductPostSearchResponse>> search(
		String query,
		String category,
		double minPrice,
		double maxPrice,
		String tags,
		Pageable pageable
	) {

		// tags 문자열 -> List<String> 변환
		List<String> tagList = tags != null && !tags.isBlank()
			? Arrays.stream(tags.split(","))
			.map(String::trim)
			.filter(tag -> !tag.isEmpty())
			.toList()
			: List.of(); // @Query에서 terms 쿼리를 항상 쓰는 구조라면 빈 리스트로 전달

		// Repository @Query 메서드 호출
		Page<ProductPostDocumentEntity> resultPage =
			productPostElasticRepository.search(query, category, minPrice, maxPrice, tagList, pageable);

		log.info("Total hits: {}, Total pages: {}",
			resultPage.getTotalElements(), resultPage.getTotalPages());

		List<ProductPostSearchResponse> productPostResponseList = resultPage
			.getContent()
			.stream()
			.map(ProductPostMapper::toProductPostSearchResponse)
			.toList();

		return new PageResponse<>(
			resultPage.getNumber(),
			resultPage.getTotalPages(),
			resultPage.getSize(),
			resultPage.hasNext(),
			productPostResponseList
		);
	}
}