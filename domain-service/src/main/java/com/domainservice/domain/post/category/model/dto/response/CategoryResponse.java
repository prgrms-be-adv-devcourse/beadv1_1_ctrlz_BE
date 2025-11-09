package com.domainservice.domain.post.category.model.dto.response;

import com.domainservice.domain.post.category.model.entity.Category;

public record CategoryResponse(
        String id,
        String name
) {
    /**
     * Entity -> DTO 변환
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}