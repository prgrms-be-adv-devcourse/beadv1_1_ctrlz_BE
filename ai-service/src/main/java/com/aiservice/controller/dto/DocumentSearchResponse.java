package com.aiservice.controller.dto;

import java.util.Map;

import lombok.Builder;

@Builder
public record DocumentSearchResponse(
	String id,
	String content,
	Map<String, Object> metadata,
	Double score
) {
}
