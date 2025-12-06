package com.aiservice.infrastructure.qdrant;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.document.Document;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.domain.repository.VectorRepository;

/**
 * 테스트용 InMemory VectorRepository 구현체
 * EmbeddingModel 없이 간단한 문자열 매칭으로 동작
 */
public class InMemoryVectorRepository implements VectorRepository {

    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, String> productIdToDocId = new ConcurrentHashMap<>();

    @Override
    public String addDocument(ProductVectorContent data) {
        String documentId = UUID.randomUUID().toString();
        String content = buildNaturalLanguageContent(data);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", data.productId());
        metadata.put("categoryName", data.categoryName());
        metadata.put("price", data.price());
        metadata.put("tags", data.tags() != null ? data.tags() : List.of());
        metadata.put("uploadTime", LocalDateTime.now().toString());

        Document document = new Document(documentId, content, metadata);
        documents.put(documentId, document);

        if (data.productId() != null) {
            productIdToDocId.put(data.productId(), documentId);
        }

        return documentId;
    }

    @Override
    public Optional<Document> findDocumentByProductId(String productId) {
        String docId = productIdToDocId.get(productId);
        if (docId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(documents.get(docId));
    }

    @Override
    public List<DocumentSearchResponse> similaritySearch(String query, int maxResults) {
        String queryLower = query.toLowerCase();

        return documents.values().stream()
                .filter(doc -> doc.getText() != null && doc.getText().toLowerCase().contains(queryLower))
                .limit(maxResults)
                .map(doc -> DocumentSearchResponse.builder()
                        .id(doc.getId())
                        .content(doc.getText())
                        .metadata(doc.getMetadata())
                        .score(calculateSimpleScore(doc.getText(), query))
                        .build())
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .toList();
    }

    @Override
    public void deleteDocument(String productId) {
        String docId = productIdToDocId.remove(productId);
        if (docId != null) {
            documents.remove(docId);
        }
    }

    /**
     * 테스트용 간단 스코어 계산 (문자열 매칭 기반)
     */
    private double calculateSimpleScore(String content, String query) {
        if (content == null || query == null)
            return 0.0;
        String contentLower = content.toLowerCase();
        String queryLower = query.toLowerCase();

        String[] queryWords = queryLower.split("\\s+");
        int matchCount = 0;
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                matchCount++;
            }
        }
        return (double) matchCount / queryWords.length;
    }

    private String buildNaturalLanguageContent(ProductVectorContent data) {
        StringBuilder content = new StringBuilder();

        if (data.title() != null && !data.title().isBlank()) {
            content.append("제품명: ").append(data.title()).append(". ");
        }
        if (data.name() != null && !data.name().isBlank()) {
            content.append("상품명: ").append(data.name()).append(". ");
        }
        if (data.description() != null && !data.description().isBlank()) {
            content.append("설명: ").append(data.description()).append(". ");
        }
        if (data.categoryName() != null && !data.categoryName().isBlank()) {
            content.append("카테고리: ").append(data.categoryName()).append(". ");
        }
        if (data.tags() != null && !data.tags().isEmpty()) {
            content.append("태그: ").append(String.join(", ", data.tags())).append(". ");
        }
        if (data.price() > 0) {
            content.append("가격: ").append(data.price()).append("원. ");
        }
        if (data.status() != null && !data.status().isBlank()) {
            content.append("상태: ").append(data.status()).append(".");
        }

        return content.toString().trim();
    }

    // 테스트 헬퍼 메서드
    public int getDocumentCount() {
        return documents.size();
    }

    public void clear() {
        documents.clear();
        productIdToDocId.clear();
    }
}
