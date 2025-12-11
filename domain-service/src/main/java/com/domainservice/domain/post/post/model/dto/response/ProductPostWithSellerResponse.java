package com.domainservice.domain.post.post.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;

import lombok.Builder;

/**
 * 단일 상품 게시글 응답 DTO (seller nickname 포함)
 */
@Builder
public record ProductPostWithSellerResponse(
	String id,
	String userId,
	String categoryId,
	String nickname,
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