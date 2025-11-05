package com.domainservice.domain.product.brand.model.dto.response;

import com.domainservice.domain.product.brand.model.entity.Brand;

public record BrandResponse(
        String id,
        String name
) {
    /**
     * Entity -> DTO 변환
     */
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName()
        );
    }
}