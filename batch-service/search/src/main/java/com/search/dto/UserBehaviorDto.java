package com.search.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record UserBehaviorDto(
                String id,
                String userId,
                String behaviorValue,
                String behaviorType,
                LocalDateTime createdAt) {
}
