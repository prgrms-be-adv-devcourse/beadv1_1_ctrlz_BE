package com.domainservice.domain.post.favorite.mapper;

import com.domainservice.domain.post.favorite.model.dto.FavoriteProductResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public class FavoriteProductMapper {

	public static FavoriteProductResponse toResponse(ProductPost productPost) {
		return FavoriteProductResponse.builder()
			.productPostId(productPost.getId())
			.title(productPost.getTitle())
			.price(productPost.getPrice())
			.tradeStatus(productPost.getTradeStatus())
			.primaryImageUrl(productPost.getPrimaryImageUrl())
			.createdAt(productPost.getCreatedAt())
			.build();
	}

}
