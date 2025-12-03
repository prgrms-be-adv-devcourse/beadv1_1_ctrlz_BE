package com.gatewayservice.utils;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import com.gatewayservice.handler.TokenType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieProvider {

	public static ResponseCookie to(String name, String accessToken, Duration duration) {
		return ResponseCookie.from(name, accessToken)
			.httpOnly(true)
			.secure(false) // SameSite=None은 Secure 필수 (HTTPS 필요) 추후 변경 예정
			.path("/")
			.maxAge(duration)
			.sameSite("Lax") // 추후 none 설정으로 Cross-Site에서도 Cookie 전송 허용
			.build();
	}

	public static ResponseCookie expireAccessToken() {
		return ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), "logout")
			.httpOnly(true)
			.secure(false) // SameSite=None은 Secure 필수 (HTTPS 필요) 추후 변경 예정
			.path("/")
			.maxAge(0)
			.sameSite("Lax")
			.build();
	}
}
