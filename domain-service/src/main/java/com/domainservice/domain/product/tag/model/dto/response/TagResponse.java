package com.domainservice.domain.product.tag.model.dto.response;

import com.domainservice.domain.product.tag.model.entity.Tag;

public record TagResponse(
        String id,
        String name
) {
    /**
     * Entity -> DTO 변환
     */
    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName()
        );
    }
}