package com.domainservice.domain.search.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostRecommendationService {

	public PageResponse<List<ProductPostSearchResponse>> findSimilarProducts(Long productPostId, Pageable pageable) {
		return null;
	}

	public PageResponse<List<ProductPostSearchResponse>> findPopularInCategory(Long productPostId, Pageable pageable) {
		return null;
	}

	public PageResponse<List<ProductPostSearchResponse>> findSellerProducts(Long productPostId, String sort, Pageable pageable) {
		return null;
	}

}
