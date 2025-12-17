package com.domainservice.domain.search.api;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.PageResponse;
import com.domainservice.common.configuration.springDoc.HidePageableSort;
import com.domainservice.domain.search.docs.GetSimilarProductsApiDocs;
import com.domainservice.domain.search.docs.GlobalSearchApiDocs;
import com.domainservice.domain.search.model.entity.dto.request.postSearchParams;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.service.search.GlobalSearchService;
import com.domainservice.domain.search.service.search.RecommendationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * elasticSearch를 사용하여 상품 게시글 검색 및 추천 기능을 제공하는 REST API 컨트롤러입니다.
 */
@Tag(name = "ProductPost Search", description = "다양한 상품 게시글 목록 조회 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts/search")
public class ProductPostSearchController {

	private final GlobalSearchService globalSearchService;
	private final RecommendationService recommendationService;

	/**
	 * 통합 검색 API
	 * - 모든 필터 조건을 단일 엔드포인트에서 처리
	 *
	 * @param searchParams 검색 파라미터
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
	@HidePageableSort
	@GlobalSearchApiDocs
	@GetMapping
	public PageResponse<List<ProductPostSearchResponse>> search(
		@Valid postSearchParams searchParams,
		@PageableDefault(size = 24) Pageable pageable
	) {
		return globalSearchService.search(searchParams, pageable);
	}

	/**
	 * 비슷한 상품 추천 API
	 * - 현재 상품과 유사한 상품 추천
	 *
	 * @param productPostId 기준 상품 ID
	 * @param pageable 페이징 정보, size: 12(default)
	 * @return 유사 상품 목록
	 */
	@GetSimilarProductsApiDocs
	@GetMapping("/{productPostId}/similar")
	public PageResponse<List<ProductPostSearchResponse>> getSimilarProducts(
		@PathVariable String productPostId,
		@PageableDefault(size = 12) Pageable pageable
	) {
		return recommendationService.findSimilarProductList(productPostId, pageable);
	}

	/**
	 * 판매자의 다른 상품 추천 API
	 * - 같은 판매자가 판매 중인 다른 상품 추천
	 *
	 * @param productPostId 기준 상품 ID
	 * @param pageable 페이징 정보, size: 12(default)
	 * @return 판매자의 다른 상품 목록
	 */
	@GetMapping("/{productPostId}/by-seller")
	public PageResponse<List<ProductPostSearchResponse>> getSellerProductList(
		@PathVariable String productPostId,
		@PageableDefault(size = 12) Pageable pageable
	) {
		return recommendationService.getSellerProductList(productPostId, pageable);
	}

	/**
	 * 오늘의 추천 상품 API
	 * - 최근 3일(72시간) 내 등록된 상품 중 인기 상품 선정
	 * - 요청 파라메터에 카테고리 입력 시 해당 카테고리의 인기상품 검색 가능
	 *
	 * @param category 카테고리명 (선택)
	 * @param pageable 페이징 정보, size: 12(default)
	 * @return 오늘의 추천 상품 목록
	 */
	@GetMapping("/daily")
	public PageResponse<List<ProductPostSearchResponse>> getDailyProductList(
		@RequestParam(defaultValue = "all") String category,
		@PageableDefault(size = 12) Pageable pageable
	) {
		return recommendationService.getDailyBestProductList(category, pageable);
	}

}