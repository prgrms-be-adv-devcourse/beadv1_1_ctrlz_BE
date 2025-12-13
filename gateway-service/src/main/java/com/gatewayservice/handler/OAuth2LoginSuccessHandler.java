package com.gatewayservice.handler;

import com.gatewayservice.client.AccountServiceClient;
import com.gatewayservice.dto.LoginRequest;
import com.gatewayservice.utils.CookieProvider;
import com.gatewayservice.utils.ServletRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

	private final AccountServiceClient accountServiceClient;
	private final UserVerificationHandler userVerificationHandler;

	@Value("${custom.redirect-url}")
	private String redirectUrl;

	@Override
	public Mono<Void> onAuthenticationSuccess(
			WebFilterExchange webFilterExchange,
			Authentication authentication) {

		log.info("OAuth2 로그인 성공 - Gateway에서 처리");

		OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
		OAuth2User oauth2User = oauth2Token.getPrincipal();
		Map<String, Object> attributes = oauth2User.getAttributes();

		String email = (String) attributes.get("email");
		String name = (String) attributes.get("name");
		String picture = (String) attributes.get("picture");
		String provider = oauth2Token.getAuthorizedClientRegistrationId();

		// IP 추출 (IpCaptureFilter 로직)
		ServerWebExchange exchange = webFilterExchange.getExchange();
		String userIp = ServletRequestUtils.extractIp(exchange.getRequest());

		return accountServiceClient.processLogin(
				LoginRequest.builder()
						.provider(provider)
						.email(email)
						.nickname(name)
						.profileImageUrl(picture)
						.build())
				.flatMap(response -> {
					log.info("Account Service 처리 완료: userId={}, isNewUser={}",
							response.userId(), response.isNewUser());

					ResponseCookie accessTokenCookie = CookieProvider.to(
							TokenType.ACCESS_TOKEN.name(),
							response.accessToken(),
							TokenType.ACCESS_TOKEN.getDuration());

					ResponseCookie refreshTokenCookie = CookieProvider.to(
							TokenType.REFRESH_TOKEN.name(),
							response.refreshToken(),
							TokenType.REFRESH_TOKEN.getDuration());

					// 쿠키 설정 주석 처리 (Cross-Domain 문제로 URL 파라미터 방식 사용)
					// exchange.getResponse().addCookie(accessTokenCookie);
					// exchange.getResponse().addCookie(refreshTokenCookie);

					// ip와 토큰 redis에 저장
					if (userIp != null && response.accessToken() != null) {
						userVerificationHandler.addTokenAndIp(response.accessToken(), userIp);
						log.info("OAuth2 로그인 - IP/Token 저장 완료: ip={}", userIp);
					}

					String targetPath = response.isNewUser() ? "/signup" : "";

					String redirectUrl = UriComponentsBuilder.fromUriString(this.redirectUrl)
							.path(targetPath)
							.queryParam("nickname", response.nickname())
							.queryParam("email", response.email())
							.queryParam("profileImage", response.profileImageUrl())
							.queryParamIfPresent("provider", Optional.ofNullable(response.provider()))
							.queryParam("accessToken", response.accessToken()) // 토큰 추가
							.queryParam("refreshToken", response.refreshToken()) // 토큰 추가
							.encode()
							.toUriString();

					log.info("리다이렉트 → {}", redirectUrl);

					exchange.getResponse().setStatusCode(HttpStatus.FOUND);
					exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));

					return exchange.getResponse().setComplete();
				})
				.onErrorResume(e -> {
					log.error("OAuth2 로그인 처리 중 오류", e);

					ServerWebExchange errorExchange = webFilterExchange.getExchange();
					errorExchange.getResponse().setStatusCode(HttpStatus.FOUND);
					errorExchange.getResponse().getHeaders().setLocation(
							URI.create(redirectUrl + "/login/error") // TODO: 로그인 페이지 변경 예정
					);
					return errorExchange.getResponse().setComplete();
				});
	}
}
