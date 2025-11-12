package com.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.TokenRefreshRequest;
import com.auth.jwt.JwtTokenProvider;
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

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthService jwtAuthService;

	@PostMapping("/refresh")
	public void refreshToken(@RequestBody TokenRefreshRequest request, HttpServletResponse response) {

		String accessToken = jwtAuthService.reissueAccessToken(request.userId(), request.refreshToken());
		ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
			.httpOnly(true)
			.secure(false)
			.path("/")
			.maxAge(jwtTokenProvider.getExpirationFromToken(accessToken).getEpochSecond())
			.sameSite("Lax")
			.build();

		response.addHeader("Set-Cookie", accessTokenCookie.toString());
	}

	@PostMapping("/logout")
	public void logout(HttpServletRequest request) {
		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
		String token = bearerToken.replace("Bearer ", "");
		String userId = jwtTokenProvider.getUserIdFromToken(token);
		jwtAuthService.logout(userId, token);
	}
}
