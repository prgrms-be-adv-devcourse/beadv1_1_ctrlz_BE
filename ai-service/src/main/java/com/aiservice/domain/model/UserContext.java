package com.aiservice.domain.model;

import java.util.List;

import lombok.Builder;

@Builder
public record UserContext(
        String gender,
        int age,
        List<String> recentSearchKeywords,
        List<String> cartProductNames) {
}
