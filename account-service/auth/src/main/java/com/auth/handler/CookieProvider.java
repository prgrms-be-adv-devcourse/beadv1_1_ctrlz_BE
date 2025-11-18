package com.auth.handler;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import com.auth.jwt.TokenType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieProvider {

	public static ResponseCookie to(String name, String accessToken, Duration duration) {
		return ResponseCookie.from(name, accessToken)
			.httpOnly(true)
			.secure(false) // 로컬환경
			.path("/")
			.maxAge(duration)
			.sameSite("Lax")
			.build();
	}

	public static ResponseCookie expireAccessToken() {
		return ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), "logout")
			.httpOnly(true)
			.secure(false) // 로컬환경
			.path("/")
			.maxAge(0)
			.sameSite("Lax")
			.build();
	}
}
