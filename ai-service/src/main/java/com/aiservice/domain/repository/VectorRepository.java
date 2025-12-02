package com.aiservice.domain.repository;

import java.util.List;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;

public interface VectorRepository {
	String addDocument(ProductVectorContent data);
	List<DocumentSearchResponse> similaritySearch(String query, int maxResults, String categoryName, List<String> tags);
}
