package com.domainservice.domain.post.post.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;

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
        String imageUrl,
        List<String> tags,
        LocalDateTime createdAt
) {
    /**
     * Entity → DTO 변환
     */
    public static ProductPostResponse from(ProductPost productPost) {
        // 태그 이름 목록 추출
        List<String> tagNames = productPost.getProductPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        return new ProductPostResponse(
                productPost.getId(),
                productPost.getUserId(),
                productPost.getCategoryId(),
                productPost.getTitle(),
                productPost.getName(),
                productPost.getPrice(),
                productPost.getDescription(),
                productPost.getStatus(),
                productPost.getTradeStatus(),
                productPost.getImageUrl(),
                tagNames,
                productPost.getCreatedAt()
        );
    }
}