package com.aiservice.infrastructure.qdrant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.domain.repository.VectorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class QdrantVectorRepository implements VectorRepository {

	private final VectorStore qdrantVectorStore;

	@Override
	public String addDocument(ProductVectorContent data) {
		String documentId = UUID.randomUUID().toString();
		String content = buildNaturalLanguageContent(data);

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("productId", data.productId());
		metadata.put("categoryName", data.categoryName());
		metadata.put("tags", data.tags().toArray(String[]::new));
		metadata.put("price", data.price());
		metadata.put("name", data.name());

		Document document = new Document(content, metadata);

		qdrantVectorStore.add(List.of(document));

		return documentId;
	}

	@Override
	public Optional<Document> findDocumentByProductId(String productId) {
		FilterExpressionBuilder filter = new FilterExpressionBuilder();
		SearchRequest request = SearchRequest.builder()
				.query("")
				.topK(3)
				.filterExpression(filter.eq("productId", productId).build())
				.build();

		List<Document> documents = qdrantVectorStore.similaritySearch(request);
		if (!documents.isEmpty()) {
			return Optional.of(documents.getFirst());
		}
		return Optional.empty();
	}

	@Override
	public List<DocumentSearchResponse> similaritySearch(String query, int maxResults) {
		log.info("유사도 검색 시작 query = {}, 최대 결과 = {}", query, maxResults);

		SearchRequest request = SearchRequest.builder()
				.query(query)
				.topK(maxResults)
				.build();

		List<Document> documents = qdrantVectorStore.similaritySearch(request);
		if (documents.isEmpty()) {
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

	@Override
	public void deleteDocument(String productId) {
		FilterExpressionBuilder filter = new FilterExpressionBuilder();
		List<Document> documents = qdrantVectorStore.similaritySearch(
				SearchRequest.builder()
						.query("")
						.topK(5) // 혹시 중복된게 있을 수 있으니 여유있게
						.filterExpression(filter.eq("productId", productId).build())
						.build());

		if (!documents.isEmpty()) {
			List<String> ids = documents.stream().map(Document::getId).toList();
			qdrantVectorStore.delete(ids);
			log.info("documentID 삭제: {}, count: {}", productId, ids.size());
		}

		log.info("삭제할 document 존재하지 않음: {}", productId);

	}

	private String buildNaturalLanguageContent(ProductVectorContent data) {

		// 글 제목
		String content = "제품명: " + data.title() + ". "

		// 상품이름 (제목과 다른 경우만)
				+ "상품명: " + data.name() + ". "

				// 상세 설명
				+ "설명: " + data.description() + ". "

				// 카테고리
				+ "카테고리: " + data.categoryName() + ". "

				// 태그
				+ "태그: " + String.join(", ", data.tags()) + ". "

				// 가격
				+ "가격: " + data.price() + "원. "

				// 상태
				+ "상태: " + data.status() + ".";

		return content.trim();
	}
}
