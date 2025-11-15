package com.domainservice.domain.search.api;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.PageResponse;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.service.ProductPostElasticService;

import lombok.RequiredArgsConstructor;

/**
 * 상품 게시글 elasticSearch 기능에 대한 REST API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts/search")
public class ProductPostSearchController {

	private final ProductPostElasticService productPostElasticService;

	/**
	 * 키워드에 따른 통합 검색 API
	 */
	@GetMapping
	public PageResponse<List<ProductPostSearchResponse>> search(
		@RequestParam String q,
		@RequestParam String category,
		@RequestParam(defaultValue = "0") double minPrice,
		@RequestParam(defaultValue = "99999999") double maxPrice,
		@RequestParam(required = false) String tags, // "tag1,tag2" 형태
		Pageable pageable
	) {
		return productPostElasticService.search(q, category, minPrice, maxPrice, tags, pageable);
	}

	// TODO: 현재 조회중인 상품과 유사한 상품 조회 API
	//  - 엔드포인트: GET /api/product-posts/search/{postId}/similar
    /*
    @GetMapping("/{postId}/similar")
    public List<ProductPostSearchResponse> getSimilarProductPosts(...) { ... }
    */

	// TODO: 다른 고객이 해당 상품과 함께 본 상품 조회 API
	//  - 엔드포인트: GET /api/product-posts/search/{postId}/viewed-together
    /*
    @GetMapping("/{postId}/also-viewed")
    public List<ProductPostSearchResponse> getAlsoViewedProductPosts(...) { ... }
    */

	// TODO: 오늘의 추천 상품 조회 API
	//  - 엔드포인트: GET /api/product-posts/search/recommendations/today
    /*
    @GetMapping("/recommendations/today")
    public List<ProductPostSearchResponse> getTodayRecommendations(...) { ... }
    */

}
