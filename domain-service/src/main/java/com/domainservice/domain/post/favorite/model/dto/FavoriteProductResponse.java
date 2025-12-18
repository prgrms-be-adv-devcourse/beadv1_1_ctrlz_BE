package com.domainservice.domain.post.favorite.model.dto;

import java.time.LocalDateTime;

import com.common.model.vo.TradeStatus;

import lombok.Builder;

@Builder
public record FavoriteProductResponse(
	String productPostId,
	String title,
	Integer price,
	TradeStatus tradeStatus,
	String primaryImageUrl,
	LocalDateTime createdAt
) {}