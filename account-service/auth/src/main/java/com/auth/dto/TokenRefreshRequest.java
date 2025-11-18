package com.auth.dto;

public record TokenRefreshRequest(
	String refreshToken,
	String userId
) {
}
