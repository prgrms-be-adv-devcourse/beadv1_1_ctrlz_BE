package com.aiservice.infrastructure;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.infrastructure.embedding.BM25SparseEmbedder;

import io.qdrant.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class QdrantVectorRepository {

	private final VectorStore vectorStore;
	private final QdrantClient qdrantClient;
	private final BM25SparseEmbedder sparseEmbedder;

	public String addDocument(ProductVectorContent data) {
		String documentId = UUID.randomUUID().toString();
		String content = buildNaturalLanguageContent(data);

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("uploadTime", LocalDateTime.now().toString());
		metadata.put("id", documentId);
		metadata.put("categoryName", data.categoryName());
		metadata.put("tags", String.join(",", data.tags()));

		Document document = new Document(content, metadata);
		vectorStore.add(List.of(document));
		return documentId;
	}

	public List<DocumentSearchResponse> similaritySearch(String query, int maxResults, String categoryName,
			List<String> tags) {
		log.info("Qdrant 유사도 검색 시작 query = {}, 최대 결과 = {}", query, maxResults);

		SearchRequest request = SearchRequest.builder()
				.query(query)
				.topK(maxResults)
				.filterExpression(new FilterExpressionBuilder()
						.in("tags", String.join(",", tags))
						.build())
				.filterExpression(
						new FilterExpressionBuilder()
								.eq("categoryName", categoryName).build())
				.build();

		List<Document> documents = vectorStore.similaritySearch(request);
		if (documents == null || documents.isEmpty()) {
			return List.of();
		}

		return documents.stream()
				.map(document -> DocumentSearchResponse.builder()
						.id(document.getId())
						.content(document.getText() == null ? "" : document.getText())
						.metadata(document.getMetadata())
						.score(document.getScore() == null ? 0 : document.getScore())
						.build())
				.toList();
	}

	// /**
	// * Hybrid Search: Dense Vector (의미 기반) + Sparse Vector (키워드 기반) 결합 검색
	// *
	// * 짧은 쿼리에서도 정확도를 높이기 위한 방법:
	// * 1. Dense Vector: 임베딩 모델을 통한 의미 기반 검색 (기존 similaritySearch)
	// * 2. Sparse Vector: BM25 기반 키워드 매칭
	// * 3. 두 결과를 Reciprocal Rank Fusion (RRF) 알고리즘으로 결합
	// *
	// * @param query 검색 쿼리
	// * @param maxResults 최대 결과 개수
	// * @param denseWeight Dense Vector 가중치 (0.0 ~ 1.0, 기본 0.7)
	// * @return 검색 결과 리스트
	// */
	// public List<DocumentSearchResponse> hybridSearch(String query, int
	// maxResults, double denseWeight) {
	// log.info("Qdrant Hybrid Search 시작: query='{}', maxResults={},
	// denseWeight={}",
	// query, maxResults, denseWeight);
	//
	// if (denseWeight < 0.0 || denseWeight > 1.0) {
	// throw new IllegalArgumentException("denseWeight는 0.0 ~ 1.0 사이여야 합니다.");
	// }
	//
	// double sparseWeight = 1.0 - denseWeight;
	//
	// // 1. Dense Vector 검색 (의미 기반)
	// List<DocumentSearchResponse> denseResults = similaritySearch(query,
	// maxResults);
	// log.info("Dense 검색 결과: {} 건", denseResults.size());
	//
	// // 2. Sparse Vector 생성 및 검색 (키워드 기반)
	// Map<Integer, Float> sparseVector =
	// sparseEmbedder.generateSparseVector(query);
	// List<DocumentSearchResponse> sparseResults =
	// performSparseSearch(sparseVector, maxResults * 2);
	// log.info("Sparse 검색 결과: {} 건", sparseResults.size());
	//
	// // 3. RRF (Reciprocal Rank Fusion)으로 결과 병합
	// Map<String, Double> fusedScores = new HashMap<>();
	// Map<String, DocumentSearchResponse> documentMap = new HashMap<>();
	//
	// // Dense 결과 처리
	// for (int i = 0; i < denseResults.size(); i++) {
	// DocumentSearchResponse doc = denseResults.get(i);
	// double rrfScore = denseWeight / (60.0 + (i + 1)); // RRF k=60
	// fusedScores.merge(doc.id(), rrfScore, Double::sum);
	// documentMap.put(doc.id(), doc);
	// }
	//
	// // Sparse 결과 처리
	// for (int i = 0; i < sparseResults.size(); i++) {
	// DocumentSearchResponse doc = sparseResults.get(i);
	// double rrfScore = sparseWeight / (60.0 + (i + 1)); // RRF k=60
	// fusedScores.merge(doc.id(), rrfScore, Double::sum);
	// documentMap.putIfAbsent(doc.id(), doc);
	// }
	//
	// // 4. 점수 기준 정렬 후 상위 N개 반환
	// List<DocumentSearchResponse> hybridResults = fusedScores.entrySet().stream()
	// .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
	// .limit(maxResults)
	// .map(entry -> {
	// DocumentSearchResponse original = documentMap.get(entry.getKey());
	// // 하이브리드 점수로 업데이트
	// return DocumentSearchResponse.builder()
	// .id(original.id())
	// .content(original.content())
	// .metadata(original.metadata())
	// .score(entry.getValue())
	// .build();
	// })
	// .toList();
	//
	// log.info("Hybrid Search 완료: {} 건 반환 (Dense {} + Sparse {} → Fused {})",
	// hybridResults.size(), denseResults.size(), sparseResults.size(),
	// fusedScores.size());
	//
	// return hybridResults;
	// }
	//
	// /**
	// * Sparse Vector를 사용한 키워드 매칭 검색
	// *
	// * Note: 실제 Qdrant Sparse Vector 검색은 Native Client를 통해 구현해야 하지만,
	// * 현재는 간단한 TF-IDF 방식으로 기존 문서와 매칭
	// */
	// private List<DocumentSearchResponse> performSparseSearch(Map<Integer, Float>
	// querySparseVector, int maxResults) {
	// // 현재는 Dense 검색으로 대체 (실제 구현 시 Qdrant Native API 사용 필요)
	// // Qdrant의 Sparse Vector는 별도의 Named Vector로 저장되어야 함
	//
	// // TODO: 실제 Qdrant Sparse Vector 검색 구현
	// // 현재는 키워드를 추출하여 메타데이터 필터링으로 대체
	//
	// log.warn("Sparse Vector 검색은 현재 Dense 검색으로 대체됩니다. " +
	// "실제 Sparse Vector 지원을 위해서는 Qdrant Collection에 Named Sparse Vector 설정이
	// 필요합니다.");
	//
	// // 임시: 빈 결과 반환 (Dense만 사용)
	// return List.of();
	// }

	private String buildNaturalLanguageContent(ProductVectorContent data) {

		StringBuilder content = new StringBuilder();

		if (data.title() != null && !data.title().isBlank()) {
			content.append(data.title()).append("\n");
		}

		if (data.name() != null && !data.name().isBlank() &&
				!data.name().equals(data.title())) {
			content.append(data.name()).append("\n");
		}

		if (data.categoryName() != null && !data.categoryName().isBlank()) {
			content.append("카테고리: ").append(data.categoryName()).append("\n");
		}

		if (data.tags() != null && !data.tags().isEmpty()) {
			content.append("\n태그: ").append(String.join(", ", data.tags()));
		}

		if (data.price() > 0) {
			content.append("가격: ").append(data.price()).append("원\n");
		}

		if (data.status() != null && !data.status().isBlank()) {
			content.append("상태: ").append(data.status()).append("\n");
		}

		return content.toString().trim();
	}
}
