package com.auth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth.jwt.JwtTokenProvider;
import com.auth.oauth2.CustomOAuth2User;
import com.auth.service.JwtAuthService;
import com.user.domain.vo.UserRole;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 로그인 성공 시 처리 핸들러
 *  - userId가 없을 경우 추가 정보 기입 페이지로 이동
 *  - 이미 가입된 회원일 경우
 * 		- JWT 토큰 발급
 * 		- 사용자 정보 추출 및 토큰 발급
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Value("${custom.redirectUrl}")
	private String redirectUrl;

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthService jwtAuthService;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException, ServletException {

		log.info("OAuth2 로그인 성공");

		try {

			CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();
			String email = customOAuth2User.email();
			String userId = customOAuth2User.userId();
			List<UserRole> roles = customOAuth2User.roles();

			if (needToSignup(request, response, customOAuth2User, email)) {
				return;
			}

			String accessToken = jwtTokenProvider.createAccessToken(userId, roles);
			String refreshToken = jwtTokenProvider.createRefreshToken(userId, roles);
			jwtAuthService.saveRefreshToken(userId, refreshToken);

			ResponseCookie accessTokenCookie = CookieProvider.to("accessToken", accessToken, Duration.ofMinutes(15));
			ResponseCookie refreshTokenCookie = CookieProvider.to("refreshToken", refreshToken, Duration.ofDays(7));

			response.addHeader("Set-Cookie", accessTokenCookie.toString());
			response.addHeader("Set-Cookie", refreshTokenCookie.toString());

			LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
			queryParams.add("profile_image", customOAuth2User.profileUrl());
			queryParams.add("nickname", customOAuth2User.nickname());

			String uriString = UriComponentsBuilder.fromUriString(redirectUrl)
				.queryParams(queryParams)
				.encode(StandardCharsets.UTF_8)
				.build()
				.toUriString();
			log.info("OAuth2 로그인 성공 - 리다이렉트");

			getRedirectStrategy().sendRedirect(request, response, uriString);

		} catch (Exception e) {
			log.error("OAuth2 로그인 처리 중 오류 발생", e);
			throw new OAuth2AuthorizationException(new OAuth2Error("로그인 실패"));
		}
	}

	private long getMaxAgeSeconds(String token) {
		Instant expiration = jwtTokenProvider.getExpirationFromToken(token);
		return Duration.between(Instant.now(), expiration).toMillis();
	}

	private boolean needToSignup(
		HttpServletRequest request,
		HttpServletResponse response,
		CustomOAuth2User customOAuth2User,
		String email
	) throws IOException {
		if (customOAuth2User.userId() == null || customOAuth2User.userId().isBlank()) {
			log.info("추가 정보 기입 필요");
			MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
			queryParams.add("email", email);

			String uriString = UriComponentsBuilder.fromUriString(redirectUrl)
				.queryParams(queryParams)
				.encode(StandardCharsets.UTF_8)
				.build()
				.toUriString();

			getRedirectStrategy().sendRedirect(request, response, uriString);
			return true;
		}
		return false;
	}
}
