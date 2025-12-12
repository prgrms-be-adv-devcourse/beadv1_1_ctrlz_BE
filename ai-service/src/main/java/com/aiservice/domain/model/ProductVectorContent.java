package com.aiservice.domain.model;

import java.util.List;

import lombok.Builder;

@Builder
public record ProductVectorContent(
		String productId,
		String title,
		String categoryName,
		String status,
		int price,
		String description,
		List<String> tags,
		String url) {
}
