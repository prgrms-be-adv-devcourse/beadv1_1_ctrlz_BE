package com.aiservice.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.document.Document;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;

public interface VectorRepository {
	String addDocument(ProductVectorContent data);
	Optional<Document> findDocumentByProductId(String productId);
	List<DocumentSearchResponse> similaritySearch(String query, int maxResults, String categoryName, List<String> tags);
	void deleteDocument(String productId);
}
