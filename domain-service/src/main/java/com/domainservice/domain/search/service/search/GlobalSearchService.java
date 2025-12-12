package com.domainservice.domain.search.service.search;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.service.search.query.GlobalSearchQueryBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

	private final ElasticsearchOperations elasticsearchOperations;

	private final GlobalSearchQueryBuilder globalSearchQueryBuilder;

	/**
	 * 상품 통합 검색 요청 메서드
	 * - 검색어(q), 카테고리, 가격 범위, 태그 등 다양한 필터 조건을 조합하여 상품을 검색
	 *
	 * @param request  검색 조건 DTO (검색어, 카테고리, 최소/최대 가격, 태그 등 포함)
	 * @param pageable 페이징 정보 (page, size, sort)
	 * @return 검색 조건에 일치하는 상품 목록과 페이징 정보가 담긴 응답 객체
	 */
	public PageResponse<List<ProductPostSearchResponse>> search(
		ProductPostSearchRequest request, Pageable pageable) {

		NativeQuery searchQuery = globalSearchQueryBuilder.build(request, pageable);

		SearchHits<ProductPostDocumentEntity> searchHits = elasticsearchOperations
			.search(searchQuery, ProductPostDocumentEntity.class);

		return SearchMapper.toPageResponse(searchHits, pageable);
	}

}
