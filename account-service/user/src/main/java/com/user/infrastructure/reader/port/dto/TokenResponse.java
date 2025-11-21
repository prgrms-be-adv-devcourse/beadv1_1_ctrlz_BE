package com.user.infrastructure.reader.port.dto;

import org.springframework.http.ResponseCookie;

public record TokenResponse(
	ResponseCookie accessToken,
	ResponseCookie refreshToken
) {
}
