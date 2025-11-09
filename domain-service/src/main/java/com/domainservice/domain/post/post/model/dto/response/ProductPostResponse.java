package com.domainservice.domain.post.post.model.dto.response;

import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 게시글 응답 DTO
 */
public record ProductPostResponse(
        String id,
        String userId,
        String categoryId,
        String title,
        String name,
        Integer price,
        String description,
        ProductStatus status,
        TradeStatus tradeStatus,
        List<String> imageUrls,
        String primaryImageUrl,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}