package com.aiservice.infrastructure.feign.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;


@Builder
public record ProductPostEsSearchResponse(
        String id,
        String name,
        String title,
        String description,
        List<String> tags,
        String categoryName,
        Long price,
        Long likedCount,
        Long viewCount,
        String status,
        String tradeStatus,
        String deleteStatus,
        LocalDateTime createdAt
) {
}
