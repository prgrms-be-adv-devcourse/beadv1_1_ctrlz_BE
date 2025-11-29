package com.auth.controller;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;
import com.auth.handler.CookieProvider;
import com.auth.jwt.JwtTokenProvider;
import com.auth.jwt.TokenType;
import com.auth.service.AuthService;
import com.auth.service.JwtAuthService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthService jwtAuthService;
	private final AuthService authService;


	@PostMapping("/login")
	public ResponseEntity<LoginResponse> googleLogin(@RequestBody LoginRequest request) {
		log.info("로그인 요청: email={}", request.email());
		
		LoginResponse response = authService.processLogin(request);
		
		return ResponseEntity.ok(response);
	}

	@PostMapping("/reissue")
	public void refreshToken(
		@RequestHeader("X-REQUEST-ID") String userId,
		@CookieValue(name = "REFRESH_TOKEN") String refreshToken,
		HttpServletResponse response
	) {
		String accessToken = jwtAuthService.reissueAccessToken(userId, refreshToken);

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
