package com.domainservice.domain.post.post.model.dto.response;

import static com.common.model.persistence.BaseEntity.*;

import java.time.LocalDateTime;
import java.util.List;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;

import lombok.Builder;

@Builder
public record ProductPostDescription(
	String id,
	String nickname,
	String name,
	String title,
	String categoryName,
	Integer price,
	String description,
	List<String> tags,
	List<String> imageUrls,
	String primaryImageUrl,
	Integer viewCount,
	Integer likedCount,
	ProductStatus status,
	TradeStatus tradeStatus,
	DeleteStatus deleteStatus,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	Boolean isMine
) {
}