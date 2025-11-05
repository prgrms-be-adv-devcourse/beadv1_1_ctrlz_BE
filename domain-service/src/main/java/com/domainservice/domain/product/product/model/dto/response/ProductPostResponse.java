package com.domainservice.domain.product.product.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.domainservice.domain.product.product.model.entity.Product;
import com.domainservice.domain.product.product.model.enums.ProductStatus;
import com.domainservice.domain.product.product.model.enums.TradeStatus;

/**
 * 상품 게시글 응답 DTO
 */
public record ProductPostResponse(
        String id,
        String userId,
        String categoryId,
        String brandId,
        String title,
        String name,
        Integer price,
        String description,
        ProductStatus status,
        TradeStatus tradeStatus,
        String imageUrl,
        List<String> tags,
        LocalDateTime createdAt
) {
    /**
     * Entity → DTO 변환
     */
    public static ProductPostResponse from(Product product) {
        // 태그 이름 목록 추출
        List<String> tagNames = product.getProductTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        return new ProductPostResponse(
                product.getId(),
                product.getUserId(),
                product.getCategoryId(),
                product.getBrandId(),
                product.getTitle(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStatus(),
                product.getTradeStatus(),
                product.getImageUrl(),
                tagNames,
                product.getCreatedAt()
        );
    }
}