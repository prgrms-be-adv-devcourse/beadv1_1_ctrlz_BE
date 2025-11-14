package com.domainservice.domain.search.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
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
	 * @param query 검색어 (상품명, 제목, 설명, 태그 등에서 검색)
	 * @param pageable 페이징 정보 (페이지 번호, 사이즈, 정렬 조건)
	 * @return 검색된 상품 목록과 페이징 정보를 포함한 PageResponse
	 */
	public PageResponse<List<ProductPostSearchResponse>> search(String query, Pageable pageable) {

		// Elasticsearch Repository를 통해 검색 쿼리 실행
		// - SearchPage: Elasticsearch 검색 결과를 담는 Spring Data의 Page 구현체
		SearchPage<ProductPostDocumentEntity> searchPage = productPostElasticRepository.search(query, pageable);

		// - getTotalElements(): 검색 조건에 맞는 전체 문서 개수
		// - getTotalPages(): 전체 페이지 수
		log.info("Total hits: {}, Total pages: {}", searchPage.getTotalElements(), searchPage.getTotalPages());

		List<ProductPostSearchResponse> productPostResponseList = searchPage.getSearchHits()
			.stream()
			.map(SearchHit::getContent)
			.map(ProductPostMapper::toProductPostSearchResponse)
			.toList();

		return new PageResponse<>(
			searchPage.getNumber(),
			searchPage.getTotalPages(),
			searchPage.getSize(),
			searchPage.hasNext(),
			productPostResponseList
		);
	}

}
