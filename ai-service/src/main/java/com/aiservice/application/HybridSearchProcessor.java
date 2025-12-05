package com.aiservice.application;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.repository.VectorRepository;
import com.aiservice.infrastructure.feign.DomainServiceClient;
import com.aiservice.infrastructure.feign.dto.PageResponse;
import com.aiservice.infrastructure.feign.dto.ProductPostEsSearchResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Elasticsearch (via domain-service) + Vector DB 하이브리드 검색 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HybridSearchProcessor {

	private final DomainServiceClient domainServiceClient;
	private final VectorRepository vectorRepository;

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

		// 3. RRF로 결과 병합
		List<DocumentSearchResponse> mergedResults = mergeWithRRF(esResults, vectorResults, limit);
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

	/**
	 * Reciprocal Rank Fusion (RRF) 알고리즘으로 결과 병합
	 * Score = sum(1 / (k + rank)) for each source
	 * k = 60 (일반적인 상수)
	 */
	private List<DocumentSearchResponse> mergeWithRRF(
		List<ProductPostEsSearchResponse> esResults,
		List<DocumentSearchResponse> vectorResults,
		int limit) {

		final int k = 60;
		Map<String, Double> rrfScores = new LinkedHashMap<>();
		Map<String, DocumentSearchResponse> docMap = new LinkedHashMap<>();

		// ES 결과 점수 계산
		for (int i = 0; i < esResults.size(); i++) {
			ProductPostEsSearchResponse esDoc = esResults.get(i);
			String productId = esDoc.id();
			double score = 1.0 / (k + i + 1);
			rrfScores.merge(productId, score, Double::sum);

			// ES 결과를 DocumentSearchResponse로 변환
			if (!docMap.containsKey(productId)) {
				docMap.put(productId, convertToDocumentSearchResponse(esDoc));
			}
		}

		// Vector 결과 점수 계산
		for (int i = 0; i < vectorResults.size(); i++) {
			DocumentSearchResponse vectorDoc = vectorResults.get(i);
			String productId = (String)vectorDoc.metadata().get("productId");
			if (productId == null)
				continue;

			double score = 1.0 / (k + i + 1);
			rrfScores.merge(productId, score, Double::sum);

			// Vector 결과가 더 상세한 정보를 가질 수 있으므로 우선 사용
			docMap.putIfAbsent(productId, vectorDoc);
		}

		// RRF 점수로 정렬
		return rrfScores.keySet().stream()
			.sorted(Comparator.comparing(rrfScores::get).reversed())
			.limit(limit)
			.map(docMap::get)
			.filter(Objects::nonNull)
			.toList();
	}

	private DocumentSearchResponse convertToDocumentSearchResponse(ProductPostEsSearchResponse esDoc) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("productId", esDoc.id());
		metadata.put("title", esDoc.title());
		metadata.put("name", esDoc.name());
		metadata.put("categoryName", esDoc.categoryName());
		metadata.put("price", esDoc.price());
		metadata.put("tags", esDoc.tags());

		return new DocumentSearchResponse(
			esDoc.id(),
			esDoc.description(),
			metadata,
			0.0
		);
	}
}
