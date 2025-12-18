package com.domainservice.domain.post.favorite.model.dto;

public record FavoritePostResponse(
	boolean isFavorite,
	String postId
) {}