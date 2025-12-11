package com.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
	String accessToken,
	String refreshToken,
	String userId,
	String email,
	String nickname,
	String provider,
	String profileImageUrl,
	boolean isNewUser
) {
}
