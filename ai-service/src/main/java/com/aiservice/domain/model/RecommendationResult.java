package com.aiservice.domain.model;

import com.aiservice.domain.vo.RecommendationStatus;

import lombok.Builder;

@Builder
public record RecommendationResult(
                RecommendationStatus status,
                String message // LLM이 생성한 전체 메시지 (상품 링크 포함)
) {

        public static RecommendationResult limitReached() {
                return RecommendationResult.builder()
                                .status(RecommendationStatus.LIMIT_REACHED)
                                .message("추천 횟수 제한에 도달했습니다.")
                                .build();
        }

        public static RecommendationResult noResults() {
                return RecommendationResult.builder()
                                .status(RecommendationStatus.NO_RESULTS)
                                .message("추천 상품을 찾지 못했습니다.")
                                .build();
        }
}
