package com.aiservice.infrastructure.feign.dto;

public record PageResponse<T>(
        int pageNum,
        int totalPages,
        int pageSize,
        boolean hasNext,
        T contents) {
}
