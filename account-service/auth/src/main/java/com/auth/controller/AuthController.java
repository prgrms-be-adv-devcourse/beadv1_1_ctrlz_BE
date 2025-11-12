package com.auth.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.TokenRefreshRequest;
import com.auth.handler.CookieProvider;
import com.auth.jwt.JwtTokenProvider;
import com.auth.jwt.TokenType;
import com.auth.service.JwtAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

	@Value("${jwt.expiration}")
	private long expiration;

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthService jwtAuthService;

	@PostMapping("/refresh")
	public void refreshToken(@RequestBody TokenRefreshRequest request, HttpServletResponse response) {

		String accessToken = jwtAuthService.reissueAccessToken(request.userId(), request.refreshToken());
		ResponseCookie responseCookie = CookieProvider.to(
			TokenType.ACCESS_TOKEN.name(),
			accessToken,
			Duration.ofMinutes(15)
		);

		response.addHeader("Set-Cookie", responseCookie.toString());
	}

	@GetMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
		String token = bearerToken.replace("Bearer ", "");
		String userId = jwtTokenProvider.getUserIdFromToken(token);
		jwtAuthService.logout(userId);

		ResponseCookie expire = CookieProvider.expireAccessToken();
		response.addHeader("Set-Cookie", expire.toString());
	}
}
