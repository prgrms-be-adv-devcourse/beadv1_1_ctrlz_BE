package com.gatewayservice.dto;

import lombok.Builder;

@Builder
public record LoginRequest(
	String email,
	String nickname,
	String profileImageUrl,
	String provider
) {
}
