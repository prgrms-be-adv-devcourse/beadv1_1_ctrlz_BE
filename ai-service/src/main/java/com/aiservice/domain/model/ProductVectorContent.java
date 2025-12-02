package com.aiservice.domain.model;

import java.util.List;

import lombok.Builder;

@Builder
public record ProductVectorContent(
	String title,
	String name,
	String categoryName,
	String status,
	int price,
	String description,
	List<String> tags
) {
}
