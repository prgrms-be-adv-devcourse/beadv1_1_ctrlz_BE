package com.aiservice.domain.model;

import java.util.List;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.vo.RecommendationStatus;

import lombok.Builder;

@Builder
public record RecommendationResult(
                RecommendationStatus status,
                String message,
                List<DocumentSearchResponse> items) {

        public static RecommendationResult limitReached() {
                return RecommendationResult.builder()
                                .status(RecommendationStatus.LIMIT_REACHED)
                                .message("추천 횟수 제한에 도달했습니다.")
                                .items(List.of())
                                .build();
        }

        public static RecommendationResult noResults() {
                return RecommendationResult.builder()
                                .status(RecommendationStatus.NO_RESULTS)
                                .message("추천 상품을 찾지 못했습니다.")
                                .items(List.of())
                                .build();
        }
}
