package com.auth.handler;

import com.auth.jwt.TokenType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieProvider {

	public static ResponseCookie to(String name, String accessToken, Duration duration) {
		return ResponseCookie.from(name, accessToken)
			.httpOnly(true)
			.secure(true) // 로컬환경
			.path("/")
			.maxAge(duration)
			.sameSite("None")
			.build();
	}

	public static ResponseCookie expireAccessToken() {
		return ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), "logout")
			.httpOnly(true)
			.secure(true) // 로컬환경
			.path("/")
			.maxAge(0)
			.sameSite("None")
			.build();
	}
}
