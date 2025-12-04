package com.domainservice.domain.post.favorite.model.dto;

public record FavoriteToggleResponse(
	boolean isFavorite,
	int likedCount
) {}