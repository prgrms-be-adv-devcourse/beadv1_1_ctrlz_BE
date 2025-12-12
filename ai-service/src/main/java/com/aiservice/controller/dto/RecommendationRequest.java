package com.aiservice.controller.dto;

public record RecommendationRequest(
        String userId,
        String query) {
}
