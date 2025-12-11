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
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.service.ProductPostElasticService;
import com.domainservice.domain.search.service.ProductPostRecommendationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상품 게시글 elasticSearch 기능에 대한 REST API를 제공하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts/search")
public class ProductPostSearchController {

	private final ProductPostElasticService productPostElasticService;
	private final ProductPostRecommendationService productPostRecommendationService;

	/**
	 * 통합 검색 API
	 * - 모든 필터 조건을 단일 엔드포인트에서 처리
	 *
	 * @param q 검색어 (optional)
	 * @param category 카테고리 (optional)
	 * @param minPrice 최소 가격 (optional, default: 0)
	 * @param maxPrice 최대 가격 (optional, default: 999999999)
	 * @param tags 태그 (optional)
	 * @param sort 정렬 기준 (optional, default: score)
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
	@GetMapping
	public PageResponse<List<ProductPostSearchResponse>> search(
		@RequestParam(required = false) String q,                 // ex) "아이폰"
		@RequestParam(required = false) String category,          // ex) "전자기기"
		@RequestParam(defaultValue = "0") Long minPrice,          // ex) "100000"
		@RequestParam(defaultValue = "999999999") Long maxPrice,  // ex) "2000000"
		@RequestParam(required = false) String tags,              // ex) "친환경,중고"
		// TODO: 상품 판매 상태에 따른 정렬,

		// ex) "score", "popular", "price_asc", "price_desc", "newest", "listing_count_desc"
		@RequestParam(defaultValue = "score") String sort,

		@PageableDefault(size = 20) Pageable pageable
	) {

		ProductPostSearchRequest request = SearchMapper.toSearchRequest(
			q, category, minPrice, maxPrice, tags, sort);

		return productPostElasticService.search(request, pageable);
	}

	/**
	 * 비슷한 상품 추천 API
	 * - 현재 상품과 유사한 상품 추천
	 *
	 * @param productPostId 기준 상품 ID
	 * @param pageable 페이징 정보, size: 10(default)
	 * @return 유사 상품 목록
	 */
	@GetMapping("/{productPostId}/similar")
	public PageResponse<List<ProductPostSearchResponse>> getSimilarProducts(
		@PathVariable String productPostId,
		@PageableDefault Pageable pageable
	) {
		return productPostRecommendationService.findSimilarProducts(productPostId, pageable);
	}

	/**
	 * 판매자의 다른 상품 추천 API
	 * - 같은 판매자가 판매 중인 다른 상품 추천
	 *
	 * @param productPostId 기준 상품 ID
	 * @param pageable 페이징 정보, size: 10(default)
	 * @return 판매자의 다른 상품 목록
	 */
	@GetMapping("/{productPostId}/by-seller")
	public PageResponse<List<ProductPostSearchResponse>> getSellerProductList(
		@PathVariable String productPostId,
		@PageableDefault Pageable pageable
	) {
		return productPostRecommendationService.getSellerProductList(productPostId, pageable);
	}

	// /**
	//  * 카테고리/태그 기반 인기 상품 추천 API
	//  * - 같은 카테고리 내에서 인기 상품 추천
	//  *
	//  * @param productPostId 기준 상품 ID
	//  * @param pageable 페이징 정보
	//  * @return 인기 상품 목록
	//  */
	// @GetMapping("/{productPostId}/popular-in-category")
	// public PageResponse<List<ProductPostSearchResponse>> getPopularInCategory(
	// 	@PathVariable Long productPostId,
	// 	@PageableDefault(size = 10) Pageable pageable
	// ) {
	// 	return productPostRecommendationService.findPopularInCategory(productPostId, pageable);
	// }

}