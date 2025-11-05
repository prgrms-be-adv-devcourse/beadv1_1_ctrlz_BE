package com.userservice.infrastructure.web.dto;

public record UserCreateResponse(
	String userId,
	String profileUrl,
	String nickname
) {
}
