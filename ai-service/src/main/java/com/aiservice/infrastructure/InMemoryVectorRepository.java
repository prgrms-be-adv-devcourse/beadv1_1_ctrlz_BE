package com.aiservice.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.domain.repository.VectorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class InMemoryVectorRepository implements VectorRepository {

	private final VectorStore vectorStore;

	@Autowired
	public InMemoryVectorRepository(EmbeddingModel embeddingModel, ObjectMapper objectMapper) {
		this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
	}

	@Override
	public String addDocument(ProductVectorContent data) {

		String documentId = UUID.randomUUID().toString();

		// JSON 대신 자연어 형태로 변환
		String content = buildNaturalLanguageContent(data);

		Map<String, Object> metadata = Map.of(
			"uploadTime", LocalDateTime.now(),
			"id", documentId
		);

		Document document = new Document(content, metadata);
		// TokenTextSplitter tokenSplitter = TokenTextSplitter.builder()
		// 	.withChunkSize(512)           // 원하는 청크 크기
		// 	.withMinChunkSizeChars(350)   // 최소 청크 크기
		// 	.withMinChunkLengthToEmbed(5) // 임베딩할 최소 청크 길이
		// 	.withMaxNumChunks(10000)      // 최대 청크 수
		// 	.withKeepSeparator(true)      // 구분자 유지 여부
		// 	.build();

		// List<Document> chunks = tokenSplitter.split(document);
		vectorStore.add(List.of(document));
		log.info("벡터 db 문서 저장 성공: {}", content);
		return documentId;
	}

	/**
	 * ProductVectorContent를 검색에 최적화된 자연어 텍스트로 변환
	 */
	private String buildNaturalLanguageContent(ProductVectorContent data) {
		StringBuilder content = new StringBuilder();

		// 제목
		if (data.title() != null && !data.title().isBlank()) {
			content.append(data.title()).append("\n");
		}

		// 이름 (제목과 다른 경우)
		if (data.name() != null && !data.name().isBlank() &&
			!data.name().equals(data.title())) {
			content.append(data.name()).append("\n");
		}

		// 카테고리
		if (data.categoryName() != null && !data.categoryName().isBlank()) {
			content.append("카테고리: ").append(data.categoryName()).append("\n");
		}

		// 가격
		if (data.price() > 0) {
			content.append("가격: ").append(data.price()).append("원\n");
		}

		// 상태
		if (data.status() != null && !data.status().isBlank()) {
			content.append("상태: ").append(data.status()).append("\n");
		}

		// 설명
		if (data.description() != null && !data.description().isBlank()) {
			content.append("\n").append(data.description()).append("\n");
		}

		// 태그
		if (data.tags() != null && !data.tags().isEmpty()) {
			content.append("\n태그: ").append(String.join(", ", data.tags()));
		}

		return content.toString().trim();
	}

	@Override
	public List<DocumentSearchResponse> similaritySearch(String query, int maxResults, String categoryName, List<String> tags) {
		log.info("유사도 검색 시작 query = {}, 최대 결과 = {}", query, maxResults);

		SearchRequest request = SearchRequest.builder()
			.query(query)
			.topK(maxResults)
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
}
