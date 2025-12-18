package com.auth.dto;

import lombok.Builder;

@Builder
public record LoginRequest(
	String email,
	String nickname,
	String profileImageUrl,
	String provider
) {
}

