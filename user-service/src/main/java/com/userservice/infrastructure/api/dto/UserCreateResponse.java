package com.userservice.infrastructure.api.dto;

public record UserCreateResponse(
	String userId,
	String profileUrl,
	String nickname
) {
}
