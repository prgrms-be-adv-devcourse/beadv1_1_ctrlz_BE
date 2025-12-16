package com.auth.controller;

import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;
import com.auth.handler.CookieProvider;
import com.auth.jwt.JwtTokenProvider;
import com.auth.jwt.TokenType;
import com.auth.service.AuthService;
import com.auth.service.JwtAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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
	public void logout(@RequestHeader("X-REQUEST-ID") String userId, HttpServletResponse response) {
		jwtAuthService.logout(userId);
		ResponseCookie expire = CookieProvider.expireAccessToken();
		response.addHeader("Set-Cookie", expire.toString());
	}
}
