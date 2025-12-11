package com.domainservice.domain.search.service.search;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.search.exception.ElasticSearchException;
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.repository.ProductPostElasticRepository;
import com.domainservice.domain.search.service.search.query.SellerProductQueryBuilder;
import com.domainservice.domain.search.service.search.query.SimilarProductQueryBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final ElasticsearchOperations elasticsearchOperations;

	private final SimilarProductQueryBuilder similarProductQueryBuilder;
	private final SellerProductQueryBuilder sellerProductQueryBuilder;

	private final ProductPostElasticRepository productPostElasticRepository;

	/**
	 * 조회한 상품과 유사한 제품 추천 메서드
	 *
	 * 주어진 상품(postId)의 데이터를 분석하여 연관성이 높은 유사 상품 목록을 검색합니다.
	 *
	 * @param postId   기준이 되는 상품 ID
	 * @param pageable 페이징 정보 (page, size, sort)
	 * @return 조건에 맞는 유사 상품 페이지 결과
	 * @throws ElasticSearchException 상품이 존재하지 않을 경우
	 */
	public PageResponse<List<ProductPostSearchResponse>> findSimilarProductList(String postId, Pageable pageable) {

		ProductPostDocumentEntity postDocument = getProductOrThrow(postId);

		// 유사 상품 검색을 위한 Native Query 생성
		NativeQuery similarSearchQuery = similarProductQueryBuilder.build(postDocument, pageable);

		// 검색 실행 및 dto로 반환
		return executeSearch(similarSearchQuery, pageable);

	}

	/**
	 * 판매자가 판매중인 다른 상품 조회 메서드
	 *
	 * 특정 상품의 판매자가 판매하고 있는 다른 상품 목록을 조회합니다.
	 *
	 * @param postId   기준이 되는 상품 ID
	 * @param pageable 페이징 정보 (page, size, sort)
	 * @return 해당 판매자의 다른 판매 상품 페이지 결과
	 * @throws ElasticSearchException 상품이 존재하지 않을 경우
	 */
	public PageResponse<List<ProductPostSearchResponse>> getSellerProductList(String postId, Pageable pageable) {

		ProductPostDocumentEntity postDocument = getProductOrThrow(postId);

		// 판매자의 다른 상품 검색을 위한 Native Query 새성
		NativeQuery sellerProductNativeQuery = sellerProductQueryBuilder.build(postDocument, pageable);

		// 검색 실행 및 dto로 반환
		return executeSearch(sellerProductNativeQuery, pageable);
	}

	/*
    ================= private Method =================
     */

	private ProductPostDocumentEntity getProductOrThrow(String postId) {
		return productPostElasticRepository.findById(postId)
			.orElseThrow(() -> new ElasticSearchException(PRODUCT_POST_NOT_FOUND));
	}

	// 생성된 검색 쿼리를 실행하고, 결과를 PageResponse로 변환하여 반환
	private PageResponse<List<ProductPostSearchResponse>> executeSearch(NativeQuery searchQuery, Pageable pageable) {

		SearchHits<ProductPostDocumentEntity> searchHits = elasticsearchOperations
			.search(searchQuery, ProductPostDocumentEntity.class);

		return SearchMapper.toPageResponse(searchHits, pageable);

	}

}