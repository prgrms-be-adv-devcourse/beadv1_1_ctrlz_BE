package com.aiservice.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.repository.VectorRepository;
import com.aiservice.infrastructure.feign.DomainServiceClient;
import com.aiservice.infrastructure.feign.dto.PageResponse;
import com.aiservice.infrastructure.feign.dto.ProductPostEsSearchResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridSearchProcessor {

	private final DomainServiceClient domainServiceClient;
	private final VectorRepository vectorRepository;
	private final RRFMerger rrfMerger;

	public List<DocumentSearchResponse> search(String query, int limit) {
		log.info("하이브리드 검색 시작(query): {}", query);

		List<DocumentSearchResponse> vectorResults = vectorRepository.similaritySearch(query, limit);
		log.info("벡터 검색 결과: {} 건", vectorResults.size());

		List<ProductPostEsSearchResponse> esResults = searchFromElasticsearch(query, limit);
		if (esResults.isEmpty()) {
			log.warn("ES 검색 실패 또는 결과 없음, 벡터 검색 결과만 사용");
			return vectorResults;
		}
		log.info("ES 검색 결과: {} 건", esResults.size());

		List<DocumentSearchResponse> mergedResults = rrfMerger.mergeWithRRF(esResults, vectorResults, limit);
		log.info("병합된 결과: {} 건", mergedResults.size());

		return mergedResults;
	}

	private List<ProductPostEsSearchResponse> searchFromElasticsearch(String query, int limit) {
		try {
			PageResponse<List<ProductPostEsSearchResponse>> response = domainServiceClient.search(query, limit);
			return response.contents() != null ? response.contents() : List.of();
		} catch (Exception e) {
			log.error("domain-service를 통한 Elasticsearch 검색 실패", e);
			return List.of();
		}
	}
}
